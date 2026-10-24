package com.oleje.transactions_statistic.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.oleje.transactions_statistic.MainActivity;
import com.oleje.transactions_statistic.R;
import com.oleje.transactions_statistic.adapter.TransactionAdapter;
import com.oleje.transactions_statistic.databinding.FragmentTransactionListBinding;
import com.oleje.transactions_statistic.model.Transaction;
import com.oleje.transactions_statistic.viewmodel.CardViewModel;
import com.oleje.transactions_statistic.viewmodel.TransactionViewModel;
import java.util.Calendar;
import java.util.List;

public class TransactionListFragment extends Fragment {

    private FragmentTransactionListBinding binding;
    private TransactionViewModel viewModel;
    private CardViewModel cardViewModel;
    private TransactionAdapter adapter;
    private ActionMode actionMode;
    private boolean showingAll = false;
    private int cardNum;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTransactionListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_delete_card).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_delete_card) {
            confirmDelete();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Видалити картку?")
                .setMessage("Ви впевнені, що хочете видалити цю картку? Транзакції залишаться.")
                .setPositiveButton("Так", (dialog, which) -> {
                    cardViewModel.deleteCard(cardNum);
                    Navigation.findNavController(requireView()).popBackStack();
                })
                .setNegativeButton("Ні", null)
                .show();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cardNum = getArguments() != null ? getArguments().getInt("cardNum") : 0;

        adapter = new TransactionAdapter();
        binding.transactionsRecyclerView.setAdapter(adapter);

        adapter.setOnTransactionClickListener(new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(Transaction transaction, int position) {
                if (adapter.isSelectionMode()) {
                    toggleSelection(transaction);
                }
            }

            @Override
            public void onTransactionLongClick(Transaction transaction, int position) {
                if (!adapter.isSelectionMode()) {
                    startSelectionMode();
                }
                toggleSelection(transaction);
            }
        });

        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        cardViewModel = new ViewModelProvider(this).get(CardViewModel.class);

        // Header setup
        viewModel.getCardWithExpenses(cardNum).observe(getViewLifecycleOwner(), cardWithExpenses -> {
            if (cardWithExpenses != null) {
                binding.cardHeader.cardNumber.setText("**** " + cardWithExpenses.card.card);
                binding.cardHeader.totalExpenses.setText(String.format("%.2f ₴", cardWithExpenses.totalExpenses));
                if (cardWithExpenses.card.imageBlob != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(cardWithExpenses.card.imageBlob, 0, cardWithExpenses.card.imageBlob.length);
                    binding.cardHeader.cardImage.setImageBitmap(bitmap);
                }
            }
        });

        loadTransactions(cardNum, adapter);

        binding.toggleTransactionsButton.setOnClickListener(v -> {
            showingAll = !showingAll;
            if (showingAll) {
                binding.toggleTransactionsButton.setText("Згорнути транзакції");
            } else {
                binding.toggleTransactionsButton.setText("Всі транзакції");
            }
            loadTransactions(cardNum, adapter);
        });
    }

    private void loadTransactions(int cardNum, TransactionAdapter adapter) {
        // Remove previous observers
        viewModel.getTransactionsForMonth(cardNum, 0, 0).removeObservers(getViewLifecycleOwner());
        viewModel.getAllTransactionsForCard(cardNum).removeObservers(getViewLifecycleOwner());

        if (showingAll) {
            viewModel.getAllTransactionsForCard(cardNum).observe(getViewLifecycleOwner(), adapter::setData);
        } else {
            // Get start and end of current month
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            long startOfMonth = calendar.getTimeInMillis();

            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            long endOfMonth = calendar.getTimeInMillis();

            viewModel.getTransactionsForMonth(cardNum, startOfMonth, endOfMonth).observe(getViewLifecycleOwner(), adapter::setData);
        }
    }

    private void startSelectionMode() {
        if (actionMode != null) return;
        
        adapter.setSelectionMode(true);
        actionMode = ((MainActivity) requireActivity()).startSupportActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_selection, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.action_delete) {
                    confirmDeleteTransactions();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                adapter.clearSelection();
                actionMode = null;
            }
        });
    }

    private void toggleSelection(Transaction transaction) {
        adapter.toggleSelection(transaction);
        int count = adapter.getSelectedCount();
        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle("Вибрано: " + count);
        }
    }

    private void confirmDeleteTransactions() {
        List<Transaction> selected = adapter.getSelectedItems();
        new AlertDialog.Builder(requireContext())
                .setTitle("Видалити транзакції?")
                .setMessage("Ви впевнені, що хочете видалити " + selected.size() + " транзакцій?")
                .setPositiveButton("Так", (dialog, which) -> {
                    viewModel.deleteTransactions(selected);
                    if (actionMode != null) actionMode.finish();
                })
                .setNegativeButton("Ні", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

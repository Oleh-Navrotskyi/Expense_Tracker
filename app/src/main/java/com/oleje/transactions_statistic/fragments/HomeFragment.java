package com.oleje.transactions_statistic.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.oleje.transactions_statistic.R;
import com.oleje.transactions_statistic.adapter.CardAdapter;
import com.oleje.transactions_statistic.databinding.FragmentHomeBinding;
import com.oleje.transactions_statistic.model.CardWithExpensesRoom;
import com.oleje.transactions_statistic.viewmodel.CardViewModel;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private CardViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_delete_card).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Navigation.findNavController(requireView()).navigate(R.id.action_global_settingsFragment);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CardViewModel.class);

        CardAdapter adapter = new CardAdapter(new CardAdapter.OnCardClickListener() {
            @Override
            public void onCardClick(int cardNum) {
                Bundle bundle = new Bundle();
                bundle.putInt("cardNum", cardNum);
                Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_transactionListFragment, bundle);
            }

            @Override
            public void onOrderChanged(List<CardWithExpensesRoom> cards) {
                for (int i = 0; i < cards.size(); i++) {
                    cards.get(i).card.orderIndex = i;
                    viewModel.updateCard(cards.get(i).card);
                }
            }
        });

        binding.cardsRecyclerView.setAdapter(adapter);

        ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                adapter.onDragFinished();
            }
        });
        touchHelper.attachToRecyclerView(binding.cardsRecyclerView);

        viewModel.cardsWithExpenses.observe(getViewLifecycleOwner(), adapter::setData);

        binding.addCardFab.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_addCardFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

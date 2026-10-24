package com.oleje.transactions_statistic.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.oleje.transactions_statistic.R;
import com.oleje.transactions_statistic.adapter.InterceptedNotificationAdapter;
import com.oleje.transactions_statistic.data.MyRoomDatabase;
import com.oleje.transactions_statistic.databinding.FragmentSettingsBinding;
import com.oleje.transactions_statistic.model.Card;
import com.oleje.transactions_statistic.model.InterceptedNotification;
import com.oleje.transactions_statistic.model.Transaction;
import com.oleje.transactions_statistic.util.NotificationReceiver;
import com.oleje.transactions_statistic.viewmodel.CardViewModel;
import com.oleje.transactions_statistic.viewmodel.TransactionViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SharedPreferences prefs;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private CardViewModel cardViewModel;
    private TransactionViewModel transactionViewModel;
    private InterceptedNotificationAdapter interceptedAdapter;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                    Toast.makeText(getContext(), "Помилка входу: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        auth = FirebaseAuth.getInstance();
        cardViewModel = new ViewModelProvider(this).get(CardViewModel.class);
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Assuming google-services plugin provides this
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        updateUI();

        binding.googleSignInButton.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) {
                googleSignInLauncher.launch(googleSignInClient.getSignInIntent());
            } else {
                auth.signOut();
                googleSignInClient.signOut();
                updateUI();
            }
        });

        binding.syncButton.setOnClickListener(v -> startSync());

        String savedAppName = prefs.getString("target_app", "Google Wallet");
        binding.appNameEditText.setText(savedAppName);

        boolean isRecording = prefs.getBoolean("record_notifications", false);
        binding.recordNotificationsSwitch.setChecked(isRecording);
        binding.recordNotificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("record_notifications", isChecked).apply();
        });

        interceptedAdapter = new InterceptedNotificationAdapter();
        binding.interceptedRecyclerView.setAdapter(interceptedAdapter);
        interceptedAdapter.setOnItemClickListener(item -> {
            binding.appNameEditText.setText(item.title); // Or package name?
            Toast.makeText(getContext(), "Назву встановлено: " + item.title, Toast.LENGTH_SHORT).show();
        });

        MyRoomDatabase.getInstance(requireContext()).getInterceptedNotificationDao().getAll()
                .observe(getViewLifecycleOwner(), notifications -> {
                    interceptedAdapter.setData(notifications);
                });

        binding.clearInterceptedButton.setOnClickListener(v -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                MyRoomDatabase.getInstance(requireContext()).getInterceptedNotificationDao().deleteAll();
            });
        });

        updatePermissionStatus();

        binding.grantPermissionButton.setOnClickListener(v -> {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        });

        binding.sendTestButton.setOnClickListener(v -> {
            String testContent = binding.testContentEditText.getText().toString();
            String titleContent = binding.testContentEditTitle.getText().toString();
            String appName = binding.appNameEditText.getText().toString();
            
            // Save current app name setting
            prefs.edit().putString("target_app", appName).apply();

            // Manually trigger the receiver logic for testing
            // NOTE: We pass title and message in correct order
            NotificationReceiver.parseAndSave(requireContext(), titleContent, testContent);
            
            Toast.makeText(getContext(), R.string.test_processed, Toast.LENGTH_SHORT).show();
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        updateUI();
                    } else {
                        Toast.makeText(getContext(), "Автентифікація не вдалася", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            binding.userEmailText.setText(user.getEmail());
            binding.googleSignInButton.setText("Вийти");
            binding.syncButton.setVisibility(View.VISIBLE);
        } else {
            binding.userEmailText.setText("Не увійшли в акаунт");
            binding.googleSignInButton.setText("Увійти через Google");
            binding.syncButton.setVisibility(View.GONE);
        }
    }

    private void startSync() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Будь ласка, спочатку увійдіть в акаунт", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(), "Початок синхронізації...", Toast.LENGTH_SHORT).show();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = user.getUid();

        new Thread(() -> {
            try {
                // 1. Sync local Cards to Firestore
                List<Card> localCards = cardViewModel.readAllDataSync();
                if (localCards != null) {
                    for (Card card : localCards) {
                        Map<String, Object> cardMap = new HashMap<>();
                        cardMap.put("card", card.card);
                        cardMap.put("orderIndex", card.orderIndex);
                        if (card.imageBlob != null) {
                            cardMap.put("imageBlob", com.google.firebase.firestore.Blob.fromBytes(card.imageBlob));
                        }
                        
                        Tasks.await(db.collection("users").document(uid).collection("cards")
                                .document(String.valueOf(card.card))
                                .set(cardMap, SetOptions.merge()));
                    }
                }

                // 2. Sync local Transactions to Firestore
                List<Transaction> localTransactions = transactionViewModel.getAllTransactionsListSync();
                if (localTransactions != null) {
                    for (Transaction t : localTransactions) {
                        Map<String, Object> tMap = new HashMap<>();
                        tMap.put("value", t.value);
                        tMap.put("date", t.date);
                        tMap.put("card", t.card);
                        tMap.put("receiver", t.receiver);

                        // Stable ID to prevent duplicates in Firestore
                        String docId = t.date.getTime() + "_" + t.card + "_" + Double.doubleToLongBits(t.value * 100);
                        Tasks.await(db.collection("users").document(uid).collection("transactions")
                                .document(docId)
                                .set(tMap, SetOptions.merge()));
                    }
                }

                // 3. Import Cards from Firestore
                QuerySnapshot remoteCards = Tasks.await(db.collection("users").document(uid).collection("cards").get());
                for (DocumentSnapshot doc : remoteCards.getDocuments()) {
                    Long cardNumLong = doc.getLong("card");
                    if (cardNumLong == null) continue;
                    int cardNum = cardNumLong.intValue();
                    
                    Card existing = cardViewModel.getCardByNumber(cardNum);
                    if (existing == null) {
                        Card newCard = new Card();
                        newCard.card = cardNum;
                        Long orderIdx = doc.getLong("orderIndex");
                        newCard.orderIndex = orderIdx != null ? orderIdx.intValue() : 0;
                        com.google.firebase.firestore.Blob blob = doc.getBlob("imageBlob");
                        if (blob != null) newCard.imageBlob = blob.toBytes();
                        cardViewModel.addCard(newCard);
                    }
                }

                // 4. Import Transactions from Firestore
                QuerySnapshot remoteTransactions = Tasks.await(db.collection("users").document(uid).collection("transactions").get());
                
                // CRITICAL: Refresh local list after possible card imports
                final List<Transaction> updatedLocals = transactionViewModel.getAllTransactionsListSync();
                
                for (DocumentSnapshot doc : remoteTransactions.getDocuments()) {
                    Double value = doc.getDouble("value");
                    java.util.Date date = doc.getDate("date");
                    Long cardLong = doc.getLong("card");
                    String receiver = doc.getString("receiver");

                    if (value == null || date == null || cardLong == null) continue;
                    int card = cardLong.intValue();

                    boolean exists = false;
                    if (updatedLocals != null) {
                        for (Transaction lt : updatedLocals) {
                            // Compare key fields to check for existence
                            if (lt.card == card && Math.abs(lt.value - value) < 0.001 && lt.date != null && lt.date.getTime() == date.getTime()) {
                                exists = true;
                                break;
                            }
                        }
                    }

                    if (!exists) {
                        Transaction newT = new Transaction();
                        newT.value = value;
                        newT.date = date;
                        newT.card = card;
                        newT.receiver = receiver;
                        transactionViewModel.addTransaction(newT);
                    }
                }

                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Синхронізацію завершено успішно", Toast.LENGTH_SHORT).show()
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Помилка синхронізації: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        }).start();
    }

    private void updatePermissionStatus() {
        if (isNotificationServiceEnabled()) {
            binding.permissionStatusText.setText(R.string.permission_granted);
            binding.permissionStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
            binding.grantPermissionButton.setVisibility(View.GONE);
        } else {
            binding.permissionStatusText.setText(R.string.permission_not_granted);
            binding.permissionStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
            binding.grantPermissionButton.setVisibility(View.VISIBLE);
        }
    }

    private boolean isNotificationServiceEnabled() {
        String pkgName = requireContext().getPackageName();
        final String flat = Settings.Secure.getString(requireContext().getContentResolver(), "enabled_notification_listeners");
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePermissionStatus();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Save app name on exit
        if (binding != null) {
            String appName = binding.appNameEditText.getText().toString();
            prefs.edit().putString("target_app", appName).apply();
        }
        binding = null;
    }
}

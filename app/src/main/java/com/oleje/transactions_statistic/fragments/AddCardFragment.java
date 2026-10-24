package com.oleje.transactions_statistic.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.oleje.transactions_statistic.databinding.FragmentAddCardBinding;
import com.oleje.transactions_statistic.model.Card;
import com.oleje.transactions_statistic.viewmodel.CardViewModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class AddCardFragment extends Fragment {

    private FragmentAddCardBinding binding;
    private CardViewModel viewModel;
    private byte[] selectedImageBytes;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    processImage(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddCardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CardViewModel.class);

        binding.selectImageButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        
        binding.saveCardButton.setOnClickListener(v -> saveCard());

        binding.cardImagePreview.setOnClickListener(v -> {
            if (selectedImageBytes == null) {
                imagePickerLauncher.launch("image/*");
            }
        });
    }

    private void processImage(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();

            // Crop to 2:1 aspect ratio and round corners
            Bitmap croppedBitmap = cropToAspectRatio(bitmap, 2.0f);
            Bitmap roundedBitmap = getRoundedCornerBitmap(croppedBitmap, 40);

            // Compress to fit 200KB
            selectedImageBytes = compressBitmap(roundedBitmap, 200 * 1024);
            
            if (selectedImageBytes != null) {
                Bitmap previewBitmap = BitmapFactory.decodeByteArray(selectedImageBytes, 0, selectedImageBytes.length);
                binding.cardImagePreview.setImageBitmap(previewBitmap);
                binding.cardImagePreview.setBackground(null);
            } else {
                Toast.makeText(getContext(), "Не вдалося обробити фото", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Помилка при завантаженні фото", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap cropToAspectRatio(Bitmap bitmap, float aspectRatio) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth, newHeight;
        int x = 0, y = 0;

        if (width > height * aspectRatio) {
            newWidth = (int) (height * aspectRatio);
            newHeight = height;
            x = (width - newWidth) / 2;
        } else {
            newWidth = width;
            newHeight = (int) (width / aspectRatio);
            y = (height - newHeight) / 2;
        }

        return Bitmap.createBitmap(bitmap, x, y, newWidth, newHeight);
    }

    private Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    private byte[] compressBitmap(Bitmap bitmap, int maxSize) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // Use PNG to preserve transparency (fixed black corners)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        
        if (stream.size() > maxSize) {
            // PNG ignores quality, so we scale down resolution to fit size
            float scale = (float) Math.sqrt((double) maxSize / stream.size());
            int newWidth = (int) (bitmap.getWidth() * scale * 0.9);
            int newHeight = (int) (bitmap.getHeight() * scale * 0.9);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            return compressBitmap(scaledBitmap, maxSize);
        }
        
        return stream.toByteArray();
    }

    private void saveCard() {
        String cardNumStr = binding.cardNumEditText.getText().toString();
        if (cardNumStr.length() != 4) {
            binding.cardNumInputLayout.setError("Введіть рівно 4 цифри");
            return;
        }

        if (selectedImageBytes == null) {
            Toast.makeText(getContext(), "Будь ласка, виберіть фото", Toast.LENGTH_SHORT).show();
            return;
        }

        int cardNum = Integer.parseInt(cardNumStr);

        new Thread(() -> {
            Card existing = viewModel.getCardByNumber(cardNum);
            if (existing != null) {
                requireActivity().runOnUiThread(() -> 
                    binding.cardNumInputLayout.setError("Картка з таким номером вже існує")
                );
            } else {
                List<Card> allCards = viewModel.readAllDataSync();
                Card card = new Card();
                card.card = cardNum;
                card.imageBlob = selectedImageBytes;
                card.orderIndex = allCards != null ? allCards.size() : 0;
                
                viewModel.addCard(card);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Картку додано", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack();
                });
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

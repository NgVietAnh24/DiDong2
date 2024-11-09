package vn.vietanhnguyen.didong2.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import vn.vietanhnguyen.didong2.activities.InvoiceDetailActivity;
import vn.vietanhnguyen.didong2.models.Invoice;
import vn.vietanhnguyen.didong2.R;
import vn.vietanhnguyen.didong2.models.Food;

public class SelectedFoodActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private LinearLayout selectedFoodLayout;
    private Button btnCancelOrder, btnPay;
    private EditText etNote;
    private TextView tvTableName;
    private ArrayList<Food> selectedFoodList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_food);

        db = FirebaseFirestore.getInstance();
        initializeViews();
        setupListeners();

        selectedFoodList = getIntent().getParcelableArrayListExtra("selectedFoodList");
        String tableName = getIntent().getStringExtra("tableName");
        tvTableName.setText("Tên bàn: " + tableName);

        if (selectedFoodList != null) {
            for (Food food : selectedFoodList) {
                addFoodView(food);
            }
        }
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnQuayLai);
        selectedFoodLayout = findViewById(R.id.selected_food_list);
        btnCancelOrder = findViewById(R.id.btnHuyDon);
        btnPay = findViewById(R.id.btnThanhToanS);
        etNote = findViewById(R.id.etGhiChu);
        tvTableName = findViewById(R.id.tvTieuDe);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnCancelOrder.setOnClickListener(v -> {
            updateTableStatus("Trống");
            Toast.makeText(this, "Đã hủy đơn", Toast.LENGTH_SHORT).show();
            finish();
        });
        btnPay.setOnClickListener(v -> createAndShowInvoice());
    }

    private void addFoodView(Food food) {
        View foodView = LayoutInflater.from(this).inflate(R.layout.selected_food_item, selectedFoodLayout, false);
        foodView.setTag(food.getId());

        ImageView imgFood = foodView.findViewById(R.id.imgMonAn);
        TextView tvFoodName = foodView.findViewById(R.id.tvTenMonAn);
        TextView tvFoodPrice = foodView.findViewById(R.id.tvGiaMonAn);
        TextView tvStatus = foodView.findViewById(R.id.tvTrangThai);
        TextView tvQuantity = foodView.findViewById(R.id.tvSoLuong);

        Glide.with(this)
                .load(food.getImage())
                .placeholder(R.drawable.upload)
                .into(imgFood);
        tvFoodName.setText(food.getName());
        // Chuyển đổi và định dạng giá tiền sang VND
        tvFoodPrice.setText(String.format("%,d VNĐ", (long)(food.getPrice() )));
        tvStatus.setText("Chưa được đặt.");
        tvQuantity.setText("1");

        setupFoodViewListeners(foodView, food, tvStatus, tvQuantity);
        selectedFoodLayout.addView(foodView);
    }

    private void setupFoodViewListeners(View foodView, Food food, TextView tvStatus, TextView tvQuantity) {

        foodView.findViewById(R.id.btnXoa).setOnClickListener(v -> {
            selectedFoodLayout.removeView(foodView);
            selectedFoodList.remove(food);
            Toast.makeText(this, "Đã xóa món " + food.getName(), Toast.LENGTH_SHORT).show();
        });

        foodView.findViewById(R.id.btnTang).setOnClickListener(v -> {
            int quantity = Integer.parseInt(tvQuantity.getText().toString()) + 1;
            tvQuantity.setText(String.valueOf(quantity));
        });

        foodView.findViewById(R.id.btnGiam).setOnClickListener(v -> {
            int quantity = Integer.parseInt(tvQuantity.getText().toString());
            if (quantity > 1) {
                tvQuantity.setText(String.valueOf(quantity - 1));
            }
        });
    }

    private void createAndShowInvoice() {
        String tableName = tvTableName.getText().toString().replace("Tên bàn: ", "");

        db.collection("tables")
                .whereEqualTo("name", tableName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot tableDoc = queryDocumentSnapshots.getDocuments().get(0);
                        String tableId = tableDoc.getId();

                        createInvoiceInFirestore(tableDoc, invoice -> {
                            Intent intent = new Intent(this, InvoiceDetailActivity.class);
                            intent.putExtra("invoiceId", invoice.getId());
                            startActivity(intent);
                            finish();
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Lỗi khi tìm thông tin bàn", e);
                    Toast.makeText(this, "Có lỗi xảy ra khi tìm thông tin bàn", Toast.LENGTH_SHORT).show();
                });
    }

    private void createInvoiceInFirestore(DocumentSnapshot tableDoc, OnInvoiceCreatedListener listener) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date now = new Date();

        Map<String, Object> invoiceData = new HashMap<>();
        invoiceData.put("ban_id", tableDoc.getId());
        invoiceData.put("nv_id", "1");
        invoiceData.put("ten_khach_hang", tableDoc.getString("reservationName"));
        invoiceData.put("so_dt", tableDoc.getString("reservationPhone"));
        invoiceData.put("ngay_tao", dateFormat.format(now));
        invoiceData.put("gio_tao", timeFormat.format(now));
        invoiceData.put("tinh_trang", "Chưa thanh toán");
        invoiceData.put("ghi_chu", etNote.getText().toString());

        double totalAmount = calculateTotalAmount();
        invoiceData.put("tong_tien", totalAmount);

        db.collection("invoices")
                .add(invoiceData)
                .addOnSuccessListener(documentReference -> {
                    String invoiceId = documentReference.getId();
                    addInvoiceItems(invoiceId, totalAmount);

                    Invoice invoice = new Invoice();
                    invoice.setId(invoiceId);
                    invoice.setBanId(tableDoc.getId());
                    invoice.setDate(dateFormat.format(now));
                    invoice.setTime(timeFormat.format(now));
                    invoice.setTotal(totalAmount);
                    invoice.setPaymentStatus("Chưa thanh toán");

                    if (listener != null) {
                        listener.onInvoiceCreated(invoice);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CreateInvoice", "Error creating invoice", e);
                    Toast.makeText(this, "Có lỗi xảy ra khi tạo hóa đơn", Toast.LENGTH_SHORT).show();
                });
    }

    interface OnInvoiceCreatedListener {
        void onInvoiceCreated(Invoice invoice);
    }

    private double calculateTotalAmount() {
        double totalAmount = 0;
        for (Food food : selectedFoodList) {
            View foodView = selectedFoodLayout.findViewWithTag(food.getId());
            if (foodView != null) {
                TextView tvQuantity = foodView.findViewById(R.id.tvSoLuong);
                int quantity = Integer.parseInt(tvQuantity.getText().toString());
                // Chuyển đổi giá tiền sang VND
                totalAmount += (food.getPrice()) * quantity;
            }
        }
        return totalAmount;
    }

    private void addInvoiceItems(String invoiceId, double totalAmount) {
        for (Food food : selectedFoodList) {
            View foodView = selectedFoodLayout.findViewWithTag(food.getId());
            if (foodView != null) {
                TextView tvQuantity = foodView.findViewById(R.id.tvSoLuong);
                int quantity = Integer.parseInt(tvQuantity.getText().toString());
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("hoa_don_id", invoiceId);
                itemData.put("ten_mon_an", food.getName());
                itemData.put("so_luong", quantity);
                // Chuyển đổi giá tiền sang VND
                itemData.put("gia", (food.getPrice()));
                db.collection("invoice_items")
                        .add(itemData)
                        .addOnFailureListener(e -> {
                            Log.e("InvoiceItems", "Error adding invoice items", e);
                        });
            }
        }
    }

    private void updateTableStatus(String status) {
        String tableName = tvTableName.getText().toString().replace("Tên bàn: ", "");
        db.collection("tables")
                .whereEqualTo("name", tableName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot tableDoc = queryDocumentSnapshots.getDocuments().get(0);
                        String tableId = tableDoc.getId();
                        Map<String, Object> tableData = new HashMap<>();
                        tableData.put("status", status);

                        db.collection("tables").document(tableId)
                                .update(tableData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firestore", "Cập nhật trạng thái bàn thành công");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Lỗi khi cập nhật trạng thái bàn", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Lỗi khi tìm thông tin bàn", e);
                });
    }
}
package vn.vietanhnguyen.didong2.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import vn.vietanhnguyen.didong2.adapters.InvoiceAdapter;
import vn.vietanhnguyen.didong2.adapters.InvoiceItemAdapter;
import vn.vietanhnguyen.didong2.models.Invoice;
import vn.vietanhnguyen.didong2.activities.manages.InvoiceListActivity;
import vn.vietanhnguyen.didong2.R;

public class InvoiceDetailActivity extends AppCompatActivity {
    private static final String TAG = "InvoiceDetailActivity";
    private FirebaseFirestore db;
    private String invoiceId;
    private Invoice selectedInvoice;

    private TextView titleTextView, timeTextView, dateTextView, totalTextView, amountReceivedTextView, changeTextView;
    private TextView customerNameTextView, customerPhoneTextView, note;
    private TextView invoiceIdTextView; // Thêm biến này
    private Button paymentButton;
    private ListView itemsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_detail);

        initializeViews();

        db = FirebaseFirestore.getInstance();

        invoiceId = getIntent().getStringExtra("invoiceId");
        Log.d(TAG, "Received invoice ID: " + invoiceId);

        if (invoiceId == null || invoiceId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin hóa đơn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadInvoiceDetails();
    }

    private void initializeViews() {
        titleTextView = findViewById(R.id.invoiceTitleTextView);
        timeTextView = findViewById(R.id.invoiceTimeTextView);
        dateTextView = findViewById(R.id.invoiceDateTextView);
        totalTextView = findViewById(R.id.totalTextView);
        amountReceivedTextView = findViewById(R.id.amountReceivedTextView);
        changeTextView = findViewById(R.id.changeTextView);
        customerNameTextView = findViewById(R.id.customerNameTextView);
        customerPhoneTextView = findViewById(R.id.customerPhoneTextView);
        invoiceIdTextView = findViewById(R.id.invoiceIdTextView); // Khởi tạo TextView cho mã hóa đơn
        paymentButton = findViewById(R.id.paymentButton);
        itemsListView = findViewById(R.id.itemsListView);
        note = findViewById(R.id.note);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());

        paymentButton.setOnClickListener(v -> showPaymentDialog());
    }

    private void loadInvoiceDetails() {
        Log.d(TAG, "Loading invoice details for ID: " + invoiceId);
        db.collection("invoices").document(invoiceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            selectedInvoice = Invoice.fromFirestore(documentSnapshot);
                            if (selectedInvoice != null) {
                                loadInvoiceItems();
                            } else {
                                throw new Exception("Failed to parse invoice");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing invoice: " + e.getMessage());
                            Toast.makeText(this, "Lỗi khi đọc thông tin hóa đơn", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy thông tin hóa đơn", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading invoice: " + e.getMessage());
                    Toast.makeText(this, "Lỗi khi tải thông tin hóa đơn", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadInvoiceItems() {
        Log.d(TAG, "Loading invoice items for invoice ID: " + invoiceId);
        db.collection("invoice_items")
                .whereEqualTo("hoa_don_id", invoiceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log .e(TAG, "No items found for this invoice");
                    } else {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String name = document.getString("ten_mon_an");
                            Long quantityLong = document.getLong("so_luong");
                            int quantity = quantityLong != null ? quantityLong.intValue() : 0;
                            Double priceDouble = document.getDouble("gia");
                            double price = priceDouble != null ? priceDouble : 0.0;

                            if (name != null) {
                                selectedInvoice.addItem(new Invoice.InvoiceItem(name, quantity, price));
                                Log.d(TAG, "Added item: " + name + ", quantity: " + quantity + ", price: " + price);
                            }
                        }
                        Log.d(TAG, "Items loaded successfully: " + selectedInvoice.getItems());
                        displayInvoiceDetails();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading invoice items", e);
                    Toast.makeText(this, "Lỗi khi tải danh sách món", Toast.LENGTH_SHORT).show();
                });
    }

    private void displayInvoiceDetails() {
        if (selectedInvoice == null) {
            Log.e(TAG, "Selected invoice is null");
            Toast.makeText(this, "Lỗi hiển thị thông tin hóa đơn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            titleTextView.setText(selectedInvoice.getTitle());
            invoiceIdTextView.setText("Mã hóa đơn: " + selectedInvoice.getId()); // Hiển thị mã hóa đơn
            timeTextView.setText(selectedInvoice.getTime() != null ? selectedInvoice.getTime() : "N/A");
            dateTextView.setText(selectedInvoice.getDate() != null ? selectedInvoice.getDate() : "N/A");
            totalTextView.setText(String.format("Tổng tiền: %,.0f VND", selectedInvoice.getTotal())); // Cập nhật định dạng tiền

            customerNameTextView.setText("Tên khách hàng: " +
                    (selectedInvoice.getCustomerName() != null ? selectedInvoice.getCustomerName() : "N/A"));
            customerPhoneTextView.setText("Số điện thoại: " +
                    (selectedInvoice.getCustomerPhone() != null ? selectedInvoice.getCustomerPhone() : "N/A"));
            note.setText("Ghi chú: " +
                    (selectedInvoice.getNote() != null ? selectedInvoice.getNote() : "N/A"));

            String paymentStatus = selectedInvoice.getPaymentStatus();
            if (paymentStatus != null && paymentStatus.equals("Đã thanh toán")) {
                paymentButton.setVisibility(View.GONE);
                amountReceivedTextView.setVisibility(View.VISIBLE);
                changeTextView.setVisibility(View.VISIBLE);
                amountReceivedTextView.setText(String.format("Tiền thu: %,.0f VND", selectedInvoice.getAmountReceived())); // Cập nhật định dạng tiền
                changeTextView.setText(String.format("Tiền dư: %,.0f VND", selectedInvoice.getChange())); // Cập nhật định dạng tiền
            } else {
                paymentButton.setVisibility(View.VISIBLE);
                amountReceivedTextView.setVisibility(View.GONE);
                changeTextView.setVisibility(View.GONE);
            }

            if (selectedInvoice.getItems() != null && !selectedInvoice.getItems().isEmpty()) {
                InvoiceItemAdapter adapter = new InvoiceItemAdapter(this, R.layout.invoice_item, selectedInvoice.getItems());
                itemsListView.setAdapter(adapter);
            } else {
                Toast.makeText(this, "Không có món ăn trong hóa đơn", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying invoice details: " + e.getMessage());
            Toast.makeText(this, "Lỗi hiển thị thông tin hóa đơn", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPaymentDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_payment);

        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            window.setAttributes(layoutParams);
        }

        TextView titleTextView = dialog.findViewById(R.id.titleTextView);
        EditText edtTienThu = dialog.findViewById(R.id.edtTienThu);
        Button btnThanhToan = dialog.findViewById(R.id.btnThanhToan);
        Button btnClose = dialog.findViewById(R.id.btnClose);
        TextView totalAmountValue = dialog.findViewById(R.id.totalAmountValue);

        titleTextView.setText("Tính tiền");
        totalAmountValue.setText(String.format("%,.0f VND", selectedInvoice.getTotal())); // Cập nhật định dạng tiền

        btnThanhToan.setOnClickListener(v -> {
            String tienThuStr = edtTienThu.getText().toString();
            if (!tienThuStr.isEmpty()) {
                double tienThu = Double.parseDouble(tienThuStr);
                if (tienThu >= selectedInvoice.getTotal()) {
                    selectedInvoice.setAmountReceived(tienThu);
                    updateInvoicePayment(tienThu);
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Số tiền không đủ", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            }
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updateInvoicePayment(double amountReceived) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("tinh_trang", "Đã thanh toán");
        updates.put("tien_thu", amountReceived);
        updates.put("tien_du", amountReceived - selectedInvoice.getTotal());

        db.collection("invoices").document(selectedInvoice.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Thanh toán thành công", Toast.LENGTH_SHORT).show();
                    selectedInvoice.setPaymentStatus("Đã thanh toán");
                    selectedInvoice.setAmountReceived(amountReceived);
                    displayInvoiceDetails();
                    updateTableStatus("Trống");

                    // Tạo Intent để chuyển sang màn hình InvoiceList
                    Intent intent = new Intent(InvoiceDetailActivity.this, InvoiceListActivity.class);
                    // Xóa tất cả các activity trước đó và đặt InvoiceList làm activity mới
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    // Kết thúc activity hiện tại
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating payment", e);
                    Toast.makeText(this, "Lỗi khi cập nhật thanh toán", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateTableStatus(String status) {
        if (selectedInvoice != null) {
            db.collection("tables")
                    .document(selectedInvoice.getBanId())
                    .update("status", status)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Trạng thái bàn đã được cập nhật thành " + status);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi khi cập nhật trạng thái bàn", e);
                    });
        }
    }

    @Override
    public void onBackPressed() {
        // Đặt kết quả trả về nếu cần
        setResult(RESULT_OK);
        super.onBackPressed();
    }
}
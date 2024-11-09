package vn.vietanhnguyen.didong2.activities.manages;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;

import vn.vietanhnguyen.didong2.activities.InvoiceDetailActivity;
import vn.vietanhnguyen.didong2.R;
import vn.vietanhnguyen.didong2.adapters.InvoiceAdapter;
import vn.vietanhnguyen.didong2.models.Invoice;

public class InvoiceListActivity extends AppCompatActivity {
    private static final String TAG = "InvoiceListActivity";

    private ListView invoiceListView;
    private ArrayList<Invoice> invoiceList;
    private InvoiceAdapter adapter;
    private FirebaseFirestore db;
    private String tableId;
    private String tableName;
    private SortType currentSortType = SortType.STATUS_TIME;
    private TextView sortTypeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_list);

        db = FirebaseFirestore.getInstance();

        // Lấy thông tin bàn từ Intent
        tableId = getIntent().getStringExtra("tableId");
        tableName = getIntent().getStringExtra("tableName");

        setupViews();
        setupListeners();
        setupSortingViews();

        // Kiểm tra xem có cần làm mới dữ liệu không
        boolean shouldRefresh = getIntent().getBooleanExtra("refresh", false);
        if (shouldRefresh) {
            refreshData();
        } else {
            // Load danh sách hóa đơn
            if (tableId != null && !tableId.isEmpty()) {
                loadInvoicesForTable();
            } else {
                loadAllInvoices();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        if (tableId != null && !tableId.isEmpty()) {
            loadInvoicesForTable();
        } else {
            loadAllInvoices();
        }
    }

    private void setupViews() {
        // Thiết lập tiêu đề
        TextView titleTextView = findViewById(R.id.titleTextView);
        titleTextView.setText(tableName != null ? "Hóa đơn của " + tableName : "Tất cả hóa đơn");

        // Thiết lập nút quay lại
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());

        // Thiết lập ListView và Adapter
        invoiceListView = findViewById(R.id.invoiceListView);
        invoiceList = new ArrayList<>();
        adapter = new InvoiceAdapter(this, R.layout.invoice_list_item, invoiceList);
        invoiceListView.setAdapter(adapter);
    }

    private void setupListeners() {
        invoiceListView.setOnItemClickListener((parent, view, position, id) -> {
            Log.d(TAG, "Item clicked at position: " + position);
            if (position >= 0 && position < invoiceList.size()) {
                Invoice selectedInvoice = invoiceList.get(position);
                if (selectedInvoice != null && selectedInvoice.getId() != null) {
                    Log.d(TAG, "Attempting to open invoice with ID: " + selectedInvoice.getId());
                    Intent intent = new Intent(this, InvoiceDetailActivity.class);
                    intent.putExtra("invoiceId", selectedInvoice.getId());
                    intent.putExtra("tableName", selectedInvoice.getTableName());
                    intent.putExtra("hoa_don_id", selectedInvoice.getHoaDonId());
                    startActivity(intent);
                } else {
                    Log.e(TAG, "Invalid invoice or invoice ID");
                    Toast.makeText(this, "Không thể mở chi tiết hóa đơn", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupSortingViews() {
        ImageButton sortButton = findViewById(R.id.sortButton);
        sortTypeTextView = findViewById(R.id.sortTypeTextView);

        sortButton.setOnClickListener(v -> showSortOptions());
        updateSortTypeDisplay();
    }

    private void showSortOptions() {
        String[] options = {
                "Theo trạng thái và thời gian",
                "Theo tên bàn",
                "Theo ngày và thời gian"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn kiểu sắp xếp")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            currentSortType = SortType.STATUS_TIME;
                            break;
                        case 1:
                            currentSortType = SortType.TABLE_NAME;
                            break;
                        case 2:
                            currentSortType = SortType.DATE_TIME;
                            break;
                    }
                    updateSortTypeDisplay();
                    sortInvoices();
                });
        builder.show();
    }

    private void updateSortTypeDisplay() {
        String sortText = "Sắp xếp: ";
        switch (currentSortType) {
            case STATUS_TIME:
                sortText += "Trạng thái & Thời gian";
                break;
            case TABLE_NAME:
                sortText += "Tên bàn";
                break;
            case DATE_TIME:
                sortText += "Ngày & Thời gian";
                break;
        }
        sortTypeTextView.setText(sortText);
    }

    private void loadInvoicesForTable() {
        Log.d(TAG, "Loading invoices for table: " + tableId);
        db.collection("invoices")
                .whereEqualTo("ban_id", tableId)
                .orderBy("ngay_tao", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> handleInvoiceQueryResult(task));
    }

    private void loadAllInvoices() {
        Log.d(TAG, "Loading all invoices");
        db.collection("invoices")
                .orderBy("ngay_tao", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> handleInvoiceQueryResult(task));
    }

    private void handleInvoiceQueryResult(Task<QuerySnapshot> task) {
        if (task.isSuccessful() && task.getResult() != null) {
            invoiceList.clear();
            for (QueryDocumentSnapshot document : task.getResult()) {
                try {
                    Invoice invoice = Invoice.fromFirestore(document);
                    if (invoice != null) {
                        String hoaDonId = document.getString("hoa_don_id");
                        if (hoaDonId == null || hoaDonId.isEmpty()) {
                            hoaDonId = "HD" + System.currentTimeMillis();
                            updateHoaDonId(document.getId(), hoaDonId);
                        }
                        invoice.setHoaDonId(hoaDonId);
                        invoiceList.add(invoice);
                        loadTableInfo(invoice);
                        Log.d(TAG, "Added invoice : " + invoice.toString());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing invoice: " + e.getMessage());
                }
            }

            sortInvoices();

            if (invoiceList.isEmpty()) {
                Toast.makeText(this, "Không có hóa đơn nào", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "Error getting documents: ", task.getException());
            Toast.makeText(this, "Lỗi khi tải danh sách hóa đơn", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateHoaDonId(String documentId, String hoaDonId) {
        db.collection("invoices").document(documentId)
                .update("hoa_don_id", hoaDonId)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "hoa_don_id updated successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating hoa_don_id", e);
                });
    }

    private void loadTableInfo(Invoice invoice) {
        if (invoice.getBanId() != null) {
            db.collection("tables").document(invoice.getBanId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String tableName = documentSnapshot.getString("name");
                            invoice.setTableName(tableName);
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error loading table info", e));
        }
    }

    private void sortInvoices() {
        switch (currentSortType) {
            case STATUS_TIME:
                sortByStatusAndTime();
                break;
            case TABLE_NAME:
                sortByTableName();
                break;
            case DATE_TIME:
                sortByDateTime();
                break;
        }
        adapter.notifyDataSetChanged();
    }

    private void sortByStatusAndTime() {
        Collections.sort(invoiceList, (i1, i2) -> {
            int statusCompare = Boolean.compare(
                    "Đã thanh toán".equals(i1.getPaymentStatus()),
                    "Đã thanh toán".equals(i2.getPaymentStatus())
            );
            if (statusCompare != 0) return statusCompare;

            int dateCompare = i2.getDate().compareTo(i1.getDate());
            if (dateCompare != 0) return dateCompare;

            return i2.getTime().compareTo(i1.getTime());
        });
    }

    private void sortByTableName() {
        Collections.sort(invoiceList, (i1, i2) -> {
            String tableName1 = i1.getTableName() != null ? i1.getTableName() : "";
            String tableName2 = i2.getTableName() != null ? i2.getTableName() : "";
            return tableName1.compareTo(tableName2);
        });
    }

    private void sortByDateTime() {
        Collections.sort(invoiceList, (i1, i2) -> {
            int dateCompare = i2.getDate().compareTo(i1.getDate());
            if (dateCompare != 0) return dateCompare;
            return i2.getTime().compareTo(i1.getTime());
        });
    }
}
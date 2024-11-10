package vn.vietanhnguyen.didong2.activities.homes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import vn.vietanhnguyen.didong2.R;
import vn.vietanhnguyen.didong2.activities.FoodMenuActivity;

public class HomeNhanVien extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private BroadcastReceiver updateTableReceiver;
    private EditText edtSearch;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_nhan_vien_layout);

        FirebaseApp.initializeApp(this);
        firestore = FirebaseFirestore.getInstance();

        edtSearch = findViewById(R.id.edtSearch);

        loadTableData();

        ImageButton backButton = findViewById(R.id.btnBack);
        backButton.setOnClickListener(v -> finish());


        // Đăng ký BroadcastReceiver
        updateTableReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("UPDATE_TABLE_STATUS")) {
                    String tableName = intent.getStringExtra("tableName");
                    String status = intent.getStringExtra("status");
                    updateTableUI(tableName, status);
                }
            }
        };

        IntentFilter filter = new IntentFilter("UPDATE_TABLE_STATUS");
        registerReceiver(updateTableReceiver, filter);

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                filterTables(editable.toString());
            }
        });
    }

    private void filterTables(String keyword) {
        firestore.collection("tables")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> filteredList = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            if (name != null && name.toLowerCase().contains(keyword.toLowerCase())) {
                                filteredList.add(document);
                            }
                        }

                        // Sắp xếp danh sách dựa trên số trong tên bàn
                        Collections.sort(filteredList, (t1, t2) -> {
                            String name1 = t1.getString("name");
                            String name2 = t2.getString("name");

                            if (name1 == null) return -1;
                            if (name2 == null) return 1;

                            try {
                                String num1 = name1.replaceAll("[^0-9]", "");
                                String num2 = name2.replaceAll("[^0-9]", "");

                                if (num1.isEmpty()) return -1;
                                if (num2.isEmpty()) return 1;

                                return Integer.compare(
                                        Integer.parseInt(num1),
                                        Integer.parseInt(num2)
                                );
                            } catch (NumberFormatException e1) {
                                return name1.compareTo(name2);
                            }
                        });

                        updateTableList(filteredList);
                    } else {
                        Log.d("filterTables", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void updateTableList(List<DocumentSnapshot> tableList) {
        LinearLayout tableListLayout = findViewById(R.id.tableListLayout);
        tableListLayout.removeAllViews();
        for (DocumentSnapshot document : tableList) {
            String tableName = document.getString("name");
            String tableDescription = document.getString("description");
            String tableStatus = document.getString("status");
            if (tableName != null && tableDescription != null && tableStatus != null) {
                addTableToLayout(tableName, tableDescription, tableStatus);
            }
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy đăng ký BroadcastReceiver
        unregisterReceiver(updateTableReceiver);
    }

    private void loadTableData() {
        LinearLayout tableListLayout = findViewById(R.id.tableListLayout);

        firestore.collection("tables")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("TableListActivity", "Lỗi lắng nghe: ", e);
                        return;
                    }

                    if (snapshots != null) {
                        tableListLayout.removeAllViews();
                        List<QueryDocumentSnapshot> tableList = new ArrayList<>();

                        for (QueryDocumentSnapshot document : snapshots) {
                            if (document.getString("name") != null) {
                                tableList.add(document);
                            }
                        }

                        Collections.sort(tableList, (t1, t2) -> {
                            String name1 = t1.getString("name");
                            String name2 = t2.getString("name");

                            if (name1 == null) return -1;
                            if (name2 == null) return 1;

                            try {
                                String num1 = name1.replaceAll("[^0-9]", "");
                                String num2 = name2.replaceAll("[^0-9]", "");

                                if (num1.isEmpty()) return -1;
                                if (num2.isEmpty()) return 1;

                                return Integer.compare(
                                        Integer.parseInt(num1),
                                        Integer.parseInt(num2)
                                );
                            } catch (NumberFormatException e1) {
                                return name1.compareTo(name2);
                            }
                        });

                        for (QueryDocumentSnapshot document : tableList) {
                            String tableName = document.getString("name");
                            String tableDescription = document.getString("description");
                            String tableStatus = document.getString("status");

                            if (tableName != null && tableDescription != null && tableStatus != null) {
                                addTableToLayout(tableName, tableDescription, tableStatus);
                            }
                        }
                    }
                });
    }

    private void addTableToLayout(String tableName, String tableDescription, String tableStatus) {
        LinearLayout tableListLayout = findViewById(R.id.tableListLayout);
        View tableView = getLayoutInflater().inflate(R.layout.item_table_list, null);

        TextView tableNameTextView = tableView.findViewById(R.id.tvTenBan);
        TextView tableDescriptionTextView = tableView.findViewById(R.id.tvMoTa);
        ImageView tableStatusImage = tableView.findViewById(R.id.vState);

        tableNameTextView.setText(tableName);
        tableDescriptionTextView.setText(tableDescription);

        // Cập nhật UI dựa trên trạng thái
        updateTableStatusUI(tableStatusImage, tableStatus);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 8);
        tableView.setLayoutParams(params);

// Trong TableListActivity.java, sửa lại phần setOnClickListener của tableView:

        tableView.setOnClickListener(v -> {
            firestore.collection("tables")
                    .whereEqualTo("name", tableName)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                            String currentStatus = document.getString("status");
                            // Sửa điều kiện kiểm tra để cho phép trạng thái "Đang sử dụng"
                            if (currentStatus != null &&
                                    (currentStatus.equals("Trống") ||
                                            currentStatus.equals("Đã đặt") ||
                                            currentStatus.equals("Đang sử dụng"))) {
                                Intent intent = new Intent(HomeNhanVien.this, FoodMenuActivity.class);
                                intent.putExtra("tableName", tableName);
                                intent.putExtra("tableDescription", tableDescription);
                                intent.putExtra("documentId", document.getId());
                                startActivity(intent);
                            } else {
                                Toast.makeText(this,
                                        "Không thể truy cập thông tin bàn!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this,
                                "Lỗi khi kiểm tra trạng thái bàn",
                                Toast.LENGTH_SHORT).show();
                        Log.e("TableList", "Error checking table status", e);
                    });
        });

        tableListLayout.addView(tableView);
    }

    // Thêm phương thức mới để cập nhật UI trạng thái bàn
    private void updateTableStatusUI(ImageView statusImage, String status) {
        switch (status) {
            case "Đã đặt":
                statusImage.setImageResource(R.drawable.rounded_trang_thai_da_dat);
                break;
            case "Đang sử dụng":
                statusImage.setImageResource(R.drawable.rounded_trang_thai_dang_su_dung);
                break;
            case "Trống":
            default:
                statusImage.setImageResource(R.drawable.rounded_trang_thai);
                break;
        }
    }

    private void updateTableUI(String tableName, String status) {
        LinearLayout tableListLayout = findViewById(R.id.tableListLayout);
        for (int i = 0; i < tableListLayout.getChildCount(); i++) {
            View tableView = tableListLayout.getChildAt(i );
            TextView tableNameTextView = tableView.findViewById(R.id.tableNameTextView);
            if (tableNameTextView.getText().toString().equals(tableName)) {
                ImageView tableStatusImage = tableView.findViewById(R.id.tableStatusImage);
                switch (status) {
                    case "Đã đặt":
                        tableStatusImage.setImageResource(R.drawable.rounded_trang_thai_da_dat);
                        break;
                    case "Đang sử dụng":
                        tableStatusImage.setImageResource(R.drawable.rounded_trang_thai_dang_su_dung);
                        break;
                    case "Trống":
                    default:
                        tableStatusImage.setImageResource(R.drawable.rounded_trang_thai);
                        break;
                }
                break;
            }
        }
    }
}
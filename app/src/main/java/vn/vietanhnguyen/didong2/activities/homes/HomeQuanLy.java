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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import vn.vietanhnguyen.didong2.R;
import vn.vietanhnguyen.didong2.activities.FoodMenuActivity;
import vn.vietanhnguyen.didong2.activities.login_forgot.Login;
import vn.vietanhnguyen.didong2.activities.manages.InvoiceListActivity;
import vn.vietanhnguyen.didong2.activities.manages.QLBan;
import vn.vietanhnguyen.didong2.activities.manages.QLMonAn;
import vn.vietanhnguyen.didong2.activities.manages.QLNhanVien;
import vn.vietanhnguyen.didong2.activities.manages.QLThongKe;

public class HomeQuanLy extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;

    private BroadcastReceiver updateTableReceiver;
    private List<QueryDocumentSnapshot> allTableList = new ArrayList<>();
    private EditText edtSearch;
    private TextView btnLogout, btn_ql_taikhoan, btn_ql_monan, btn_ql_ban, btn_ql_hoa_don, btn_thong_ke;
    private ImageView imgSkill, btnBack;
    private DrawerLayout drawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_quan_ly_layout);
        Event();

        FirebaseApp.initializeApp(this);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Đọc dữ liệu bàn
        loadTableData();

        // Xử lý sự kiện mở/đóng DrawerLayout
        imgSkill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
            }
        });

        btn_ql_taikhoan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeQuanLy.this, QLNhanVien.class);
                startActivity(intent);
            }
        });

        btn_ql_monan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeQuanLy.this, QLMonAn.class);
                startActivity(intent);
            }
        });

        btn_ql_ban.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeQuanLy.this, QLBan.class);
                startActivity(intent);
            }
        });

        btn_ql_hoa_don.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeQuanLy.this, InvoiceListActivity.class);
                startActivity(intent);
            }
        });

        btn_thong_ke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeQuanLy.this, QLThongKe.class);
                startActivity(intent);
            }
        });

        // Xử lý sự kiện đăng xuất
        btnLogout.setOnClickListener(view -> {
            mAuth.signOut();
            // Chuyển người dùng về màn hình đăng nhập
            Intent intent = new Intent(HomeQuanLy.this, Login.class);
            startActivity(intent);
            finish();  // Đóng màn hình hiện tại
        });

        // Xử lý tìm kiếm
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


    }

    private void loadTableData() {
        firestore.collection("tables")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("TableListActivity", "Lỗi lắng nghe: ", e);
                        return;
                    }

                    if (snapshots != null) {
                        // Xóa dữ liệu cũ và cập nhật danh sách allTableList
                        allTableList.clear();
                        for (DocumentSnapshot document : snapshots.getDocuments()) {
                            if (document instanceof QueryDocumentSnapshot) {
                                allTableList.add((QueryDocumentSnapshot) document);
                            }
                        }

                        // Lọc danh sách bàn nếu có từ khóa tìm kiếm
                        String searchQuery = edtSearch.getText().toString().trim().toLowerCase();
                        List<QueryDocumentSnapshot> displayList;
                        if (searchQuery.isEmpty()) {
                            // Không có từ khóa tìm kiếm: hiển thị toàn bộ danh sách
                            displayList = new ArrayList<>(allTableList);
                        } else {
                            // Có từ khóa tìm kiếm: lọc danh sách bàn
                            displayList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : allTableList) {
                                String tableName = document.getString("name");
                                if (tableName != null && tableName.toLowerCase().contains(searchQuery)) {
                                    displayList.add(document);
                                }
                            }
                        }

                        // Sắp xếp danh sách (dù có hay không có tìm kiếm)
                        Collections.sort(displayList, (t1, t2) -> {
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

                        // Xóa các bàn cũ và thêm bàn mới đã lọc và sắp xếp
                        LinearLayout tableListLayout = findViewById(R.id.tableListLayout);
                        tableListLayout.removeAllViews();
                        for (QueryDocumentSnapshot document : displayList) {
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



    private void filterTables(String query) {
        LinearLayout tableListLayout = findViewById(R.id.tableListLayout);
        tableListLayout.removeAllViews(); // Clear the current view

        for (QueryDocumentSnapshot document : allTableList) {
            String tableName = document.getString("name");

            if (tableName != null && tableName.toLowerCase().contains(query.toLowerCase())) {
                String tableDescription = document.getString("description");
                String tableStatus = document.getString("status");
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
                                Intent intent = new Intent(HomeQuanLy.this, FoodMenuActivity.class);
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
        if (tableListLayout == null) {
            // Log lỗi hoặc xử lý khi không tìm thấy layout
            return;
        }

        for (int i = 0; i < tableListLayout.getChildCount(); i++) {
            View tableView = tableListLayout.getChildAt(i);

            TextView tableNameTextView = tableView.findViewById(R.id.tableNameTextView);
            if (tableNameTextView == null) {
                continue; // Bỏ qua tableView này nếu không tìm thấy tableNameTextView
            }

            if (tableNameTextView.getText().toString().equals(tableName)) {
                ImageView tableStatusImage = tableView.findViewById(R.id.tableStatusImage);
                if (tableStatusImage == null) {
                    continue; // Bỏ qua tableView này nếu không tìm thấy tableStatusImage
                }

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



    public void Event() {
        drawerLayout = findViewById(R.id.drawer_layout);
        imgSkill = findViewById(R.id.btnSkill);
        btnBack = findViewById(R.id.btnBack);
        btnLogout = findViewById(R.id.btnlogout);
        btn_ql_taikhoan = findViewById(R.id.btn_ql_taikhoan);
        btn_ql_monan = findViewById(R.id.btn_ql_monan);
        btn_ql_ban = findViewById(R.id.btn_ql_ban);
        btn_ql_hoa_don = findViewById(R.id.btn_ql_hoadon);
        btn_thong_ke = findViewById(R.id.btn_thongke);
        edtSearch = findViewById(R.id.edtSearch);
    }

}

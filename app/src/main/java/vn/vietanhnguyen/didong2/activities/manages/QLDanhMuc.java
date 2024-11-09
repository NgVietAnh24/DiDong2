package vn.vietanhnguyen.didong2.activities.manages;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.vietanhnguyen.didong2.R;
import vn.vietanhnguyen.didong2.adapters.DanhMucAdapter;
import vn.vietanhnguyen.didong2.models.DanhMuc;

public class QLDanhMuc extends AppCompatActivity {

    private EditText editTextCategoryName;
    private Button buttonSave, buttonEdit;
    private ImageButton backButton;
    private ListView listViewCategories;

    private List<DanhMuc> categoryList;
    private DanhMucAdapter categoryAdapter;
    private int selectedPosition = -1;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ql_danh_muc_layout);

        db = FirebaseFirestore.getInstance();

        editTextCategoryName = findViewById(R.id.editTextCategoryName);
        buttonSave = findViewById(R.id.buttonSave);
        buttonEdit = findViewById(R.id.buttonEdit);
        backButton = findViewById(R.id.backButton);
        listViewCategories = findViewById(R.id.listViewCategories);

        categoryList = new ArrayList<>();
        categoryAdapter = new DanhMucAdapter(this, categoryList);
        listViewCategories.setAdapter(categoryAdapter);

        loadCategories();

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String categoryName = editTextCategoryName.getText().toString().trim();

                if (!categoryName.isEmpty()) {
                    addCategory(categoryName);
                    editTextCategoryName.setText("");
                } else {
                    Toast.makeText(QLDanhMuc.this, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPosition >= 0) {
                    String categoryName = editTextCategoryName.getText().toString().trim();
                    if (!categoryName.isEmpty()) {
                        updateCategory(selectedPosition, categoryName);
                        editTextCategoryName.setText("");
                    } else {
                        Toast.makeText(QLDanhMuc.this, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(QLDanhMuc.this, "Vui lòng chọn danh mục để sửa", Toast.LENGTH_SHORT).show();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        listViewCategories.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPosition = position;
                editTextCategoryName.setText(categoryList.get(position).getName());
            }
        });
    }

    // Hàm thêm danh mục mới
    private void addCategory(String categoryName) {
        Map<String, Object> category = new HashMap<>();
        category.put("name", categoryName);

        db.collection("categories")
                .add(category)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            String documentId = task.getResult().getId();
                            DanhMuc newCategory = new DanhMuc(documentId, categoryName);
                            categoryList.add(newCategory);
                            categoryAdapter.notifyDataSetChanged();
                            Toast.makeText(QLDanhMuc.this, "Đã lưu danh mục: " + categoryName, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(QLDanhMuc.this, "Lỗi khi lưu danh mục", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Hàm cập nhật danh mục
    private void updateCategory(int position, String categoryName) {
        DanhMuc category = categoryList.get(position);
        String documentId = category.getId();

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", categoryName);

        db.collection("categories")
                .document(documentId)
                .update(updates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            category.setName(categoryName);
                            categoryAdapter.notifyDataSetChanged();
                            Toast.makeText(QLDanhMuc.this, "Đã sửa danh mục: " + categoryName, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(QLDanhMuc.this, "Lỗi khi sửa danh mục", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Hàm đọc dữ liệu từ Firestore
    private void loadCategories() {
        db.collection("categories")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            categoryList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String documentId = document.getId();
                                String categoryName = document.getString("name");
                                DanhMuc category = new DanhMuc(documentId, categoryName);
                                categoryList.add(category);
                            }
                            categoryAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(QLDanhMuc.this, "Lỗi khi tải danh mục", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
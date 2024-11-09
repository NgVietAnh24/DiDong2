package vn.vietanhnguyen.didong2.activities.manages;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import vn.vietanhnguyen.didong2.R;
import vn.vietanhnguyen.didong2.models.Ban;

public class QLBan extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private EditText editTextTableName, editTextTableDescription;
    private LinearLayout tableListLayout;
    private Button addButton, editButton;
    private View currentSelectedTable;
    private List<View> tableViewsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.ql_ban_layout);

        firestore = FirebaseFirestore.getInstance();

        editTextTableName = findViewById(R.id.editTextTableName);
        editTextTableDescription = findViewById(R.id.editTextTableDescription);
        tableListLayout = findViewById(R.id.tableListLayout1);
        addButton = findViewById(R.id.addButton);
        editButton = findViewById(R.id.editButton);

        loadTableData();

        addButton.setOnClickListener(v -> addTable());
        editButton.setOnClickListener(v -> editTable());

        ImageButton backButton = findViewById(R.id.backButtonBan);
        backButton.setOnClickListener(v -> finish());
    }

    private void addTable() {
        String tableName = editTextTableName.getText().toString().trim();
        String tableDescription = editTextTableDescription.getText().toString().trim();

        if (tableName.isEmpty() || tableDescription.isEmpty()) {
            showToast("Vui lòng nhập đủ thông tin");
            return;
        }

        // Kiểm tra và định dạng tên bàn
        if (!isValidTableName(tableName)) {
            showToast("Tên bàn phải có định dạng 'Bàn X' hoặc 'Ban X' với X là số");
            return;
        }

        // Chuẩn hóa tên bàn
        tableName = formatTableName(tableName);

        saveTableToFirestore(tableName, tableDescription);

        clearInputFields();
    }

    private boolean isValidTableName(String tableName) {
        // Kiểm tra tên bàn có đúng định dạng không
        return tableName.matches("(?i)(ban|bàn)\\s*\\d+");
    }

    private String formatTableName(String tableName) {
        // Trích xuất số từ tên bàn
        String number = tableName.replaceAll("[^0-9]", "");

        // Định dạng lại tên bàn
        return "Bàn " + number;
    }

    private void saveTableToFirestore(String tableName, String tableDescription) {
        Ban table = new Ban(tableName, tableDescription, "Trống");

        firestore.collection("tables")
                .add(table)
                .addOnSuccessListener(documentReference -> {
                    String generatedId = documentReference.getId();
                    table.setId(generatedId);

                    documentReference.update("id", generatedId)
                            .addOnSuccessListener(aVoid -> {
//                                Toast.makeText(QLBan.this, "Lưu thành công với ID: " + generatedId, Toast.LENGTH_SHORT).show();
                                Log.d("Firestore", "DocumentSnapshot successfully written with ID: " + generatedId);
                            })
                            .addOnFailureListener(e -> {
                                Log.w("Firestore", "Error updating document with ID", e);
                            });

                    addTableToLayout(tableName, tableDescription, generatedId);
                    showToast("Đã thêm bàn thành công");
                })
                .addOnFailureListener(e -> {
                    showToast("Lỗi khi thêm bàn: " + e.getMessage());
                    Log.e("FirestoreError", "Lỗi khi thêm bàn: ", e);
                });
    }

    private void addTableToLayout(String tableName, String tableDescription, String documentId) {
        if (tableName == null || tableDescription == null || documentId == null) {
            Log.e("MainActivity", "Không thể thêm bàn: một hoặc nhiều giá trị là null");
            return;
        }

        View tableView = getLayoutInflater().inflate(R.layout.item_table, null);
        TextView tableNameTextView = tableView.findViewById(R.id.tableNameTextV);
        TextView tableDescriptionTextView = tableView.findViewById(R.id.tableDescriptionTV);
        Button deleteButton = tableView.findViewById(R.id.deleteButton);

        tableNameTextView.setText(tableName);
        tableDescriptionTextView.setText(tableDescription);

        tableView.setTag(documentId);
        deleteButton.setOnClickListener(v -> deleteTable(tableView));
        tableView.setOnClickListener(v -> selectTableForEditing(tableView, tableName, tableDescription));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 8);
        tableView.setLayoutParams(params);

        tableViewsList.add(tableView);
        updateTableLayout();
    }

    private void selectTableForEditing(View tableView, String tableName, String tableDescription) {
        currentSelectedTable = tableView;
        editTextTableName.setText(tableName);
        editTextTableDescription.setText(tableDescription);
        addButton.setEnabled(false);
        addButton.setAlpha(0.5f);
        editButton.setEnabled(true);
        editButton.setAlpha(1);
    }

    private void editTable() {
        if (currentSelectedTable == null) {
            showToast("Vui lòng chọn bàn để sửa");
            return;
        }

        String newTableName = editTextTableName.getText().toString().trim();
        String newTableDescription = editTextTableDescription.getText().toString().trim();

        if (newTableName.isEmpty() || newTableDescription.isEmpty()) {
            showToast("Vui lòng nhập đủ thông tin");
            return;
        }

        // Kiểm tra và định dạng tên bàn khi sửa
        if (!isValidTableName(newTableName)) {
            showToast("Tên bàn phải có định dạng 'Bàn X' hoặc 'Ban X' với X là số");
            return;
        }

        newTableName = formatTableName(newTableName);
        String documentId = (String) currentSelectedTable.getTag();

        String finalNewTableName = newTableName;
        firestore.collection("tables").document(documentId)
                .update("name", newTableName, "description", newTableDescription)
                .addOnSuccessListener(aVoid -> {
                    TextView tableNameTextView = currentSelectedTable.findViewById(R.id.tableNameTextV);
                    TextView tableDescriptionTextView = currentSelectedTable.findViewById(R.id.tableDescriptionTV);

                    tableNameTextView.setText(finalNewTableName);
                    tableDescriptionTextView.setText(newTableDescription);

                    int index = tableViewsList.indexOf(currentSelectedTable);
                    if (index != -1) {
                        View updatedTableView = currentSelectedTable;
                        tableViewsList.set(index, updatedTableView);
                    }

                    updateTableLayout();
                    clearInputFields();
                    currentSelectedTable = null;
                    showToast("Đã sửa bàn thành công");
                })
                .addOnFailureListener(e -> showToast("Lỗi khi sửa bàn: " + e.getMessage()));
    }

    private void loadTableData() {
        firestore.collection("tables")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Tạo danh sách tạm để sắp xếp
                        List<DocumentSnapshot> documents = new ArrayList<>(task.getResult().getDocuments());

                        // Sắp xếp documents theo tên bàn
                        Collections.sort(documents, (doc1, doc2) -> {
                            String name1 = doc1.getString("name");
                            String name2 = doc2.getString("name");

                            if (name1 == null || name2 == null) {
                                return 0;
                            }

                            try {
                                String num1 = name1.replaceAll("[^0-9]", "");
                                String num2 = name2.replaceAll("[^0-9]", "");

                                if (num1.isEmpty() || num2.isEmpty()) {
                                    return name1.compareTo(name2);
                                }

                                return Integer.compare(
                                        Integer.parseInt(num1),
                                        Integer.parseInt(num2)
                                );
                            } catch (NumberFormatException e) {
                                return name1.compareTo(name2);
                            }
                        });

                        // Thêm các bàn đã sắp xếp vào layout
                        for (DocumentSnapshot document : documents) {
                            String tableName = document.getString("name");
                            String tableDescription = document.getString("description");
                            String documentId = document.getId();

                            if (tableName != null && tableDescription != null) {
                                addTableToLayout(tableName, tableDescription, documentId);
                            }
                        }
                    } else {
                        showToast("Lỗi khi tải dữ liệu: " + task.getException().getMessage());
                    }
                });
    }

    private void deleteTable(View tableView) {
        String documentId = (String) tableView.getTag();

        firestore.collection("tables").document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    tableViewsList.remove(tableView);
                    updateTableLayout();
                    clearInputFields();
                    showToast("Đã xóa bàn thành công");
                })
                .addOnFailureListener(e -> showToast("Lỗi khi xóa bàn: " + e.getMessage()));
    }

    private void updateTableLayout() {
        // Sắp xếp danh sách bàn theo số thứ tự
        Collections.sort(tableViewsList, (view1, view2) -> {
            TextView name1 = view1.findViewById(R.id.tableNameTextV);
            TextView name2 = view2.findViewById(R.id.tableNameTextV);

            String tableName1 = name1.getText().toString();
            String tableName2 = name2.getText().toString();

            // Trích xuất số từ tên bàn
            try {
                String num1 = tableName1.replaceAll("[^0-9]", "");
                String num2 = tableName2.replaceAll("[^0-9]", "");

                if (num1.isEmpty() || num2.isEmpty()) {
                    return tableName1.compareTo(tableName2);
                }

                int number1 = Integer.parseInt(num1);
                int number2 = Integer.parseInt(num2);

                return Integer.compare(number1, number2);
            } catch (NumberFormatException e) {
                return tableName1.compareTo(tableName2);
            }
        });

        // Cập nhật layout
        tableListLayout.removeAllViews();
        for (View tableView : tableViewsList) {
            tableListLayout.addView(tableView);
        }
    }

    private void clearInputFields() {
        editTextTableName.setText("");
        editTextTableDescription.setText("");
        editButton.setEnabled(false);
        editButton.setAlpha(0.5f);
        addButton.setEnabled(true);
        addButton.setAlpha(1);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
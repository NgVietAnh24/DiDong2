package vn.vietanhnguyen.didong2.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.vietanhnguyen.didong2.R;
import vn.vietanhnguyen.didong2.adapters.FoodAdapter;
import vn.vietanhnguyen.didong2.models.Food;

public class FoodMenuActivity extends AppCompatActivity implements FoodAdapter.OnFoodClickListener {

    private EditText searchFood;
    private Button btnAll, btnHotpot, btnGrill, btnDrinks;
    private RecyclerView recyclerViewFoods;
    private TextView tableNameTextView;
    private TextView tableDescriptionTextView;
    private Button btnSelectedFood;

    private List<Food> foodList;
    private List<Food> selectedFoodList;
    private FoodAdapter foodAdapter;

    private String reservationName = "";
    private String reservationPhone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_menu);

        initializeViews();
        setupRecyclerView();
        getIntentData();
        loadFoodsFromFirebase();
        setupListeners();
    }

    private void initializeViews() {
        tableNameTextView = findViewById(R.id.tableNameTextView);
        tableDescriptionTextView = findViewById(R.id.tableDescriptionTextView);
        searchFood = findViewById(R.id.edtSearch);
        btnAll = findViewById(R.id.btn_all);
        btnHotpot = findViewById(R.id.btn_hotpot);
        btnGrill = findViewById(R.id.btn_grill);
        btnDrinks = findViewById(R.id.btn_drinks);
        recyclerViewFoods = findViewById(R.id.recyclerViewFoods);
        btnSelectedFood = findViewById(R.id.btn_selected_items);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        foodList = new ArrayList<>();
        selectedFoodList = new ArrayList<>();
        foodAdapter = new FoodAdapter(foodList, this, this);
        recyclerViewFoods.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewFoods.setAdapter(foodAdapter);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            String tableName = intent.getStringExtra("tableName");
            String tableDescription = intent.getStringExtra("tableDescription");

            if (tableName != null) {
                tableNameTextView.setText(tableName);
            }
            if (tableDescription != null) {
                tableDescriptionTextView.setText(tableDescription);
            }

            loadTableReservationInfo(tableName);
        }
    }

    private void loadTableReservationInfo(String tableName) {
        FirebaseFirestore.getInstance()
                .collection("tables")
                .whereEqualTo("name", tableName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        reservationName = document.getString("reservationName");
                        reservationPhone = document.getString("reservationPhone");
                    }
                });
    }

    private void loadFoodsFromFirebase() {
        FirebaseFirestore.getInstance()
                .collection("foods")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    foodList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            String id = document.getId();
                            String name = document.getString("name");
                            String image = document.getString("image");
                            String categoryId = document.getString("category_id");
                            String categoryName = document.getString("category_name");

                            // Xử lý price
                            double price = 0.0;
                            Object priceObj = document.get("price");
                            if (priceObj != null) {
                                if (priceObj instanceof Double) {
                                    price = (Double) priceObj;
                                } else if (priceObj instanceof Long) {
                                    price = ((Long) priceObj).doubleValue();
                                } else if (priceObj instanceof String) {
                                    try {
                                        price = Double.parseDouble((String) priceObj);
                                    } catch (NumberFormatException e) {
                                        Log.e("Firestore", "Error parsing price", e);
                                    }
                                }
                            }

                            // Log để debug
                            Log.d("FoodDebug", "Loading food: " +
                                    "\nID: " + id +
                                    "\nName: " + name +
                                    "\nPrice: " + price +
                                    "\nCategory ID: " + categoryId +
                                    "\nCategory Name: " + categoryName);

                            Food food = new Food(id, name, price, image, categoryId, categoryName);
                            foodList.add(food);
                        } catch (Exception e) {
                            Log.e("FoodDebug", "Error loading food: " + e.getMessage());
                        }
                    }
                    Log.d("FoodDebug", "Total foods loaded: " + foodList.size());
                    foodAdapter.updateData(foodList);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error loading foods", e);
                    Toast.makeText(this, "Lỗi khi tải danh sách món ăn",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void setupListeners() {
        btnAll.setOnClickListener(v -> {
            Log.d("ButtonClick", "All button clicked");
            foodAdapter.updateData(foodList);
        });

        btnHotpot.setOnClickListener(v -> {
            Log.d("ButtonClick", "Hotpot button clicked");
            List<Food> filtered = filterFoodByCategory("Món Lẩu");
            foodAdapter.updateData(filtered);
        });

        btnGrill.setOnClickListener(v -> {
            Log.d("ButtonClick", "Grill button clicked");
            List<Food> filtered = filterFoodByCategory("Đồ Nướng");
            foodAdapter.updateData(filtered);
        });

        btnDrinks.setOnClickListener(v -> {
            Log.d("ButtonClick", "Drinks button clicked");
            List<Food> filtered = filterFoodByCategory("Đồ uống");
            foodAdapter.updateData(filtered);
        });

        btnSelectedFood.setOnClickListener(v -> handleSelectedFood());
        Button btnReserve = findViewById(R.id.btn_reserve);
        btnReserve.setOnClickListener(v -> showReserveDialog());

        searchFood.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                filterFoodByName(s.toString());
            }
        });
    }

    // Hàm lọc theo tên món ăn
    private void filterFoodByName(String query) {
        List<Food> filteredList = new ArrayList<>();
        for (Food food : foodList) {
            if (food.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(food);
            }
        }
        foodAdapter.updateData(filteredList);
    }


    private void handleSelectedFood() {
        String tableName = tableNameTextView.getText().toString();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("tables")
                .whereEqualTo("name", tableName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("status", "Đang sử dụng");

                        firestore.collection("tables")
                                .document(documentId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firestore", "Trạng thái bàn đã được cập nhật thành đang sử dụng");

                                    Intent broadcastIntent = new Intent("UPDATE_TABLE_STATUS");
                                    broadcastIntent.putExtra("tableName", tableName);
                                    broadcastIntent.putExtra("status", "Đang sử dụng");
                                    sendBroadcast(broadcastIntent);

                                    Intent intentSelectedFood = new Intent(FoodMenuActivity.this, SelectedFoodActivity.class);
                                    intentSelectedFood.putParcelableArrayListExtra("selectedFoodList", (ArrayList<? extends Parcelable>) selectedFoodList);
                                    intentSelectedFood.putExtra("tableName", tableName);
                                    startActivity(intentSelectedFood);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Lỗi khi cập nhật trạng thái bàn", e);
                                    Toast.makeText(FoodMenuActivity.this,
                                            "Có lỗi xảy ra khi cập nhật trạng thái bàn",
                                            Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tìm thông tin bàn",
                            Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error finding table", e);
                });
    }

    private List<Food> filterFoodByCategory(String categoryName) {
        List<Food> filteredList = new ArrayList<>();
        Log.d("FilterDebug", "Filtering for category: " + categoryName);
        Log.d("FilterDebug", "Total foods before filter: " + foodList.size());

        for (Food food : foodList) {
            String foodCategoryName = food.getCategoryName();
            Log.d("FilterDebug", "Checking food: " + food.getName() +
                    " with category: " + foodCategoryName);

            if (foodCategoryName != null && foodCategoryName.equals(categoryName)) {
                filteredList.add(food);
                Log.d("FilterDebug", "Added food to filtered list: " + food.getName());
            }
        }

        Log.d("FilterDebug", "Filtered list size: " + filteredList.size());
        return filteredList;
    }

    private void showReserveDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_reserve);

        EditText editTextReservationName = dialog.findViewById(R.id.edit_text_reservation_name);
        EditText editTextReservationPhone = dialog.findViewById(R.id.edit_text_reservation_phone);
        Button buttonSave = dialog.findViewById(R.id.button_save);
        Button buttonCancel = dialog.findViewById(R.id.button_cancel);

        editTextReservationName.setText(reservationName);
        editTextReservationPhone.setText(reservationPhone);

        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        buttonSave.setOnClickListener(v -> {
            String name = editTextReservationName.getText().toString();
            String phone = editTextReservationPhone.getText().toString();

            if (!name.isEmpty() && !phone.isEmpty()) {
                updateTableStatus("Đã đặt", name, phone);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Vui lòng nhập tên và số điện thoại.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        buttonCancel.setOnClickListener(v -> {
            updateTableStatus("Trống", "", "");
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onFoodClick(Food food) {
        // Xử lý khi một món ăn được click
        if (!selectedFoodList.contains(food)) {
            selectedFoodList.add(food);
            Toast.makeText(this, "Đã thêm " + food.getName() + " vào danh sách", Toast.LENGTH_SHORT).show();
        } else {
            selectedFoodList.remove(food);
            Toast.makeText(this, "Đã xóa " + food.getName() + " khỏi danh sách", Toast.LENGTH_SHORT).show();
        }

        // Cập nhật số lượng món ăn đã chọn trên button (nếu cần)
        updateSelectedFoodCount();
    }

    private void updateSelectedFoodCount() {
        if (btnSelectedFood != null) {
            String buttonText = "Món đã chọn (" + selectedFoodList.size() + ")";
            btnSelectedFood.setText(buttonText);
        }
    }

    private void updateTableStatus(String status, String name, String phone) {
        String tableName = tableNameTextView.getText().toString();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("tables")
                .whereEqualTo("name", tableName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("status", status);
                        updates.put("reservationName", name);
                        updates.put("reservationPhone", phone);

                        firestore.collection("tables")
                                .document(documentId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firestore", "Trạng thái bàn đã được cập nhật");
                                    String message = status.equals("Trống") ?
                                            "Đã hủy đặt bàn!" :
                                            "Đặt bàn thành công!";
                                    Toast.makeText(FoodMenuActivity.this, message,
                                            Toast.LENGTH_SHORT).show();

                                    reservationName = name;
                                    reservationPhone = phone;

                                    Intent intent = new Intent("UPDATE_TABLE_STATUS");
                                    intent.putExtra("tableName", tableName);
                                    intent.putExtra("status", status);
                                    sendBroadcast(intent);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Lỗi khi cập nhật trạng thái bàn", e);
                                    Toast.makeText(FoodMenuActivity.this,
                                            "Có lỗi xảy ra khi thực hiện thao tác",
                                            Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tìm thông tin bàn",
                            Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error finding table", e);
                });
    }
}
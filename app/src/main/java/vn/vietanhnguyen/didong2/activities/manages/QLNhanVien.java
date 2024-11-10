package vn.vietanhnguyen.didong2.activities.manages;

import static android.content.ContentValues.TAG;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.vietanhnguyen.didong2.activities.homes.HomeQuanLy;
import vn.vietanhnguyen.didong2.R;
import vn.vietanhnguyen.didong2.adapters.NguoiDungAdapter;
import vn.vietanhnguyen.didong2.models.NguoiDung;

public class QLNhanVien extends AppCompatActivity {
    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    StorageReference storageReference;
    FirebaseStorage storage;
    List<NguoiDung> list;
    private NguoiDungAdapter nguoiDungAdapter;
    private RecyclerView listNhanVien;
    private Spinner spVaiTro;
    public AppCompatButton btnThem, btnSua;
    private ImageView imgMatTruocCCCD, imgMatSauCCCD, btnBack, imgEye;
    private TextInputEditText edtTenDangNhap, edtTenNhanVien, edtMatKhau, edtSoDT, edtSoCCCD, edtNgayCap;
    private SwipeRefreshLayout refreshLayout;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private Uri imageUri;
    private Uri imageUri1;
    private int currentImageView = 0;

    private boolean isPasswordVisible = false;
    private String selectedUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ql_nhanvien_layout);
        Event();
        // Khởi tạo biến thư viện Firebase
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storageReference = storage.getReference("images");
        list = new ArrayList<>();
        // Khởi tạo Adapter và gán cho RecyclerView
//        nguoiDungAdapter = new NguoiDungAdapter(list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        listNhanVien.setLayoutManager(layoutManager);
        listNhanVien.setAdapter(nguoiDungAdapter);
        Log.d("JJ","getData: " + list);
//        ghiDulieu();

        docDulieu();

        btnThem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edtTenDangNhap.getText().toString();
                String name = edtTenNhanVien.getText().toString();
                String password = edtMatKhau.getText().toString();
                String soDT = edtSoDT.getText().toString();
                String soCCCD = edtSoCCCD.getText().toString();
                String ngayCap = edtNgayCap.getText().toString();
                String selectedRole = spVaiTro.getSelectedItem().toString();
                Uri imgMatTruoc = imageUri;
                Uri imgMatSau = imageUri1;
                // Ràng buộc cho các trường nhập dữ liệu
                if (email.equals("") || name.equals("") || password.equals("") || soDT.equals("") || soCCCD.equals("") || ngayCap.equals("")) {
                    Toast.makeText(QLNhanVien.this, "Vui lòng nhập đầy đủ!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isValidEmail(email)) {
                    Toast.makeText(QLNhanVien.this, "Địa chỉ email không hợp lệ!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (name.length() > 0 && !Character.isUpperCase(name.charAt(0))) {
                    Toast.makeText(QLNhanVien.this, "Họ tên phải bắt đầu bằng chữ cái viết hoa!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.length() < 6 || !Character.isUpperCase(password.charAt(0))) {
                    Toast.makeText(QLNhanVien.this, "Mật khẩu phải có ít nhất 6 kí tự và viết hoa chữ cái đầu tiên!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (soDT.length() > 11 || soDT.charAt(0) != '0') {
                    Toast.makeText(QLNhanVien.this, "Số điện thoại không hợp lệ!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (soCCCD.length() > 12) {
                    Toast.makeText(QLNhanVien.this, "Số căn cước công dân không hợp lệ!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isValidDate(ngayCap)) {
                    Toast.makeText(QLNhanVien.this, "Không đúng định dạng dd/MM/yyyy !", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (imgMatTruoc == null || imgMatSau == null) {
                    Toast.makeText(QLNhanVien.this, "Thiếu ảnh", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Lấy thời gian hiện tại và định dạng nó thành chuỗi ngày giờ theo ý muốn
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                String ngayTao = sdf.format(new Date());
                String ngayCapNhat = sdf.format(new Date());

                int role = 0;

                // Gán role dựa trên giá trị của Spinner
                if (selectedRole.equals("Phục vụ")) {
                    role = 1;
                } else if (selectedRole.equals("Quản lý")) {
                    role = 0;
                }

                // Đăng ký người dùng vào Firebase Authentication
                int finalRole = role;
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(QLNhanVien.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        String uid = user.getUid(); // Lấy UID từ Firebase Authentication

                                        // Tạo thông tin người dùng trong Firestore với UID làm ID tài liệu
                                        NguoiDung newUser = new NguoiDung(email, name, soDT, soCCCD, ngayCap, finalRole, ngayTao, ngayCapNhat);
                                        newUser.setUid(uid);
                                        firestore.collection("users").document(uid)
                                                .set(newUser)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            // Gọi uploadImageToFirebaseStorage ở đây
                                                            uploadImageToFirebaseStorage(uid);
                                                            docDulieu();
                                                            resetInputFields();
                                                            Toast.makeText(QLNhanVien.this, "Thêm thành công ☑️", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Log.d(TAG, "Error adding document", task.getException());
                                                        }
                                                    }
                                                });
                                    }

                                } else {
                                    Toast.makeText(QLNhanVien.this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "Error registering user", task.getException());
                                }
                            }
                        });
            }
        });

        btnSua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = edtTenNhanVien.getText().toString();
                String soDT = edtSoDT.getText().toString();
                String soCCCD = edtSoCCCD.getText().toString();
                String ngayCap = edtNgayCap.getText().toString();
                String selectedRole = spVaiTro.getSelectedItem().toString();
                Uri img = imageUri;

                // Kiểm tra các điều kiện hợp lệ cho dữ liệu
                if (name.equals("") || soDT.equals("") || soCCCD.equals("") || ngayCap.equals("")) {
                    Toast.makeText(QLNhanVien.this, "Vui lòng nhập đầy đủ!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (name.length() > 0 && !Character.isUpperCase(name.charAt(0))) {
                    Toast.makeText(QLNhanVien.this, "Họ tên phải bắt đầu bằng chữ cái viết hoa!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (soDT.length() > 11 || soDT.charAt(0) != '0') {
                    Toast.makeText(QLNhanVien.this, "Số điện thoại không hợp lệ!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (soCCCD.length() > 12) {
                    Toast.makeText(QLNhanVien.this, "Số căn cước công dân không hợp lệ!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isValidDate(ngayCap)) {
                    Toast.makeText(QLNhanVien.this, "Không đúng định dạng dd/MM/yyyy !", Toast.LENGTH_SHORT).show();
                    return;
                }

                String currentUserId = selectedUser;
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                String ngayCapNhat = sdf.format(new Date());

                int role = 0;
                if (selectedRole.equals("Phục vụ")) {
                    role = 1;
                } else if (selectedRole.equals("Quản lý")) {
                    role = 0;
                }

                Map<String, Object> updates = new HashMap<>();
                updates.put("tenNhanVien", name);
                updates.put("sDT", soDT);
                updates.put("soCCCD", soCCCD);
                updates.put("ngayCap", ngayCap);
                updates.put("roles", role);
                updates.put("vaiTro", selectedRole);
                updates.put("ngayCapNhat", ngayCapNhat);

                firestore.collection("users").document(currentUserId)
                        .update(updates)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(QLNhanVien.this, "Cập nhật thành công ☑️", Toast.LENGTH_SHORT).show();
                                docDulieu();
                                resetInputFields();

                                // Đặt lại vị trí đã chọn và cập nhật RecyclerView để đổi màu về mặc định
                                nguoiDungAdapter.selectedPosition = -1;
                                nguoiDungAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(QLNhanVien.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Error updating document", task.getException());
                            }
                        });

                if (img != null) {
                    uploadImageToFirebaseStorage(currentUserId);
                }
            }
        });


        nguoiDungAdapter = new NguoiDungAdapter(list, new NguoiDungAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(NguoiDung user) {
                displayUserData(user);// Hiển thị dữ liệu của người dùng
            }
        });

        listNhanVien.setAdapter(nguoiDungAdapter);


        // Tạo danh sách dữ liệu cho Spinner
        ArrayList<String> spArray = new ArrayList<>();
        spArray.add("Quản lý");
        spArray.add("Phục vụ");


        // Tạo Adapter cho Spinner
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, spArray);

        // Gán Adapter cho Spinner
        spVaiTro.setAdapter(spinnerArrayAdapter);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(QLNhanVien.this, HomeQuanLy.class);
                startActivity(intent);
                finish();
            }
        });

        imgMatTruocCCCD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageSourceDialog(1);
            }
        });

        imgMatSauCCCD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageSourceDialog(2);
            }
        });
        // refresh lại dữ liệu
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                docDulieu();
                refreshLayout.setRefreshing(false);
            }
        });

        refreshLayout.setColorSchemeResources(
                R.color.textColorTitle,
                R.color.textColor,
                R.color.textRole
        );

        imgEye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togglePasswordVisibility();
            }
        });


    }

    public void resetInputFields() {
        edtTenDangNhap.setText("");
        edtTenDangNhap.setEnabled(true);
        edtTenNhanVien.setText("");
        edtMatKhau.setText("");
        edtMatKhau.setEnabled(true);
        edtSoDT.setText("");
        edtSoCCCD.setText("");
        edtNgayCap.setText("");
        imgMatTruocCCCD.setImageResource(R.drawable.add_card);
        imgMatSauCCCD.setImageResource(R.drawable.add_card);
        spVaiTro.setSelection(0);
        btnThem.setEnabled(true);
        btnThem.setAlpha(1);
        btnSua.setEnabled(false);
        btnSua.setAlpha(0.5f);

    }


    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        sdf.setLenient(false);

        try {
            // Thử parse chuỗi ngày
            sdf.parse(dateStr);
            return true; // Ngày hợp lệ
        } catch (ParseException e) {
            return false; // Ngày không hợp lệ
        }
    }

    // Hiển thị dữ liệu của người dùng được chọn vào các trường nhập liệu
    private void displayUserData(NguoiDung user) {
        selectedUser = user.getUid();
        edtTenDangNhap.setText(user.getTenDangNhap());
        edtTenDangNhap.setEnabled(false);
        edtTenNhanVien.setText(user.getTenNhanVien());
        edtMatKhau.setEnabled(false);
        edtSoDT.setText(user.getsDT());
        edtSoCCCD.setText(user.getSoCCCD());
        edtNgayCap.setText(user.getNgayCap());
        btnSua.setEnabled(true);
        btnSua.setAlpha(1);

        // Tải ảnh mặt trước và mặt sau từ URL trong Firestore và hiển thị vào ImageView
        if (user.getMatTruocCCCD() != null) {
            Glide.with(this).load(user.getMatTruocCCCD()).into(imgMatTruocCCCD);
        } else {
            imgMatTruocCCCD.setImageResource(R.drawable.add_card);
        }
        if (user.getMatSauCCCD() != null) {
            Glide.with(this).load(user.getMatSauCCCD()).into(imgMatSauCCCD);
        } else {
            imgMatSauCCCD.setImageResource(R.drawable.add_card);
        }

        // Chọn vai trò từ Spinner
        if (user.getVaiTro() != "Quản lý") {
            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spVaiTro.getAdapter();
            int spinnerPosition = adapter.getPosition(user.getVaiTro());
            spVaiTro.setSelection(spinnerPosition);
            spVaiTro.setEnabled(true);
            spVaiTro.setAlpha(1f);
//        Log.d("Role","Vai tro: "+ user.getVaiTro());
        } else {
            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spVaiTro.getAdapter();
            int spinnerPosition = adapter.getPosition(user.getVaiTro());
            spVaiTro.setSelection(spinnerPosition);
            spVaiTro.setEnabled(false);
            spVaiTro.setAlpha(0.5f);
        }
    }


    private void showImageSourceDialog(int imageViewIndex) {
        currentImageView = imageViewIndex; // Ghi lại ImageView hiện tại
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn hình ảnh từ");
        builder.setItems(new CharSequence[]{"Thư viện", "Camera"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    chooseImageFromGallery();
                } else {
                    openCamera();
                }
            }
        });
        builder.show();
    }


    //TODO Chọn ảnh từ thư viện
    private void chooseImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE_REQUEST);
    }

    // Mở camera
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                // Lấy URI ảnh từ thư viện
                Uri selectedImageUri = data.getData();

                if (currentImageView == 1) {
                    // Nếu là ImageView của mặt trước CCCD
                    imageUri = selectedImageUri; // Cập nhật cho mặt trước
                    imgMatTruocCCCD.setImageURI(imageUri); // Hiển thị ảnh
                } else if (currentImageView == 2) {
                    // Nếu là ImageView của mặt sau CCCD
                    imageUri1 = selectedImageUri; // Cập nhật cho mặt sau
                    imgMatSauCCCD.setImageURI(imageUri1); // Hiển thị ảnh
                }
            } else if (requestCode == CAMERA_REQUEST && data != null) {
                // Lấy ảnh từ camera dưới dạng Bitmap
                Bitmap photo = (Bitmap) data.getExtras().get("data");

                if (currentImageView == 1) {
                    // Nếu là ImageView của mặt trước CCCD
                    imageUri = getImageUriFromBitmap(photo); // Cập nhật cho mặt trước
                    imgMatTruocCCCD.setImageURI(imageUri); // Hiển thị ảnh
                } else if (currentImageView == 2) {
                    // Nếu là ImageView của mặt sau CCCD
                    imageUri1 = getImageUriFromBitmap(photo); // Cập nhật cho mặt sau
                    imgMatSauCCCD.setImageURI(imageUri1); // Hiển thị ảnh
                }
            }
        }
    }


    //TODO Chuyển Bitmap sang Uri
    private Uri getImageUriFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Camera Image", null);
        return Uri.parse(path);
    }

    private void uploadImageToFirebaseStorage(String userId) {
        // Kiểm tra từng ảnh một và upload nếu không null
        if (imageUri != null) {
            // Ảnh mặt trước
            StorageReference frontImageRef = storageReference.child("images/" + userId + "_front.jpg");
            uploadImage(frontImageRef, userId, true); // Gọi hàm upload cho ảnh mặt trước
        }

        if (imageUri1 != null) {
            // Ảnh mặt sau
            StorageReference backImageRef = storageReference.child("images/" + userId + "_back.jpg");
            uploadImage(backImageRef, userId, false); // Gọi hàm upload cho ảnh mặt sau
        } else {
//            Toast.makeText(this, "Không có ảnh được chọn", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage(StorageReference fileRef, String userId, boolean isFrontImage) {
        Uri uploadUri = isFrontImage ? imageUri : imageUri1; // Chọn URI dựa trên ảnh trước hay sau
        NguoiDung user = new NguoiDung();

        // Upload ảnh
        fileRef.putFile(uploadUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Lấy URL của ảnh sau khi upload thành công
                        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();

                                // Cập nhật URL vào object NguoiDung trước khi lưu vào Firestore
                                if (isFrontImage) {
                                    user.setMatTruocCCCD(imageUrl);

                                    firestore.collection("users").document(userId)
                                            .update("matTruocCCCD", user.getMatTruocCCCD()) // Sử dụng giá trị từ class NguoiDung
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(QLNhanVien.this, "Upload ảnh mặt trước thành công", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(QLNhanVien.this, "Upload ảnh mặt trước thất bại", Toast.LENGTH_SHORT).show();
                                            });
                                } else {
                                    // Cập nhật URL ảnh mặt sau trong object NguoiDung
                                    user.setMatSauCCCD(imageUrl);

                                    // Lưu ảnh mặt sau vào Firestore
                                    firestore.collection("users").document(userId)
                                            .update("matSauCCCD", user.getMatSauCCCD()) // Sử dụng giá trị từ class NguoiDung
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(QLNhanVien.this, "Upload ảnh mặt sau thành công", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(QLNhanVien.this, "Upload ảnh mặt sau thất bại", Toast.LENGTH_SHORT).show();
                                            });
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(QLNhanVien.this, "Upload thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void ghiDulieu() {
        CollectionReference users = firestore.collection("nguoidung");

        Map<String, Object> user = new HashMap<>();
        user.put("TenDangNhap", "Anh244@gmail.com");
        user.put("TenNhanVien", "Nguyễn Việt Anh");
        user.put("SoDT", "035637012");
        user.put("SoCCCD", "037204000021");
        user.put("NgayCap", "24/8");
//        users.document("SE").set(user);
//        Log.e("AAA", "ghiDulieu: " + user);
//
//        Map<String, Object> data2 = new HashMap<>();
//        data2.put("TenDangNhap", "Anh244@gmail.com");
//        data2.put("TenNhanVien", "Việt Anh");
//        data2.put("SoDT", 0365637012);
//        data2.put("SoCCCD",037204000021);
//        data2.put("NgayCap", "24/9");
        // Create a new user with a first and last name
//        Map<String, Object> user = new HashMap<>();
//        user.put("first", "Ada");
//        user.put("last", "Lovelace");
//        user.put("born", 1815);

// Add a new document with a generated ID
        firestore.collection("nguoidungs")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });


    }

    public void docDulieu() {
        firestore.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            list.clear(); // Xóa dữ liệu cũ
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                NguoiDung userData = document.toObject(NguoiDung.class);
                                list.add(userData);
                            }
                            Log.d(TAG, "Getting documents: ");
                            nguoiDungAdapter.notifyDataSetChanged(); // Cập nhật dữ liệu mới
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Ẩn mật khẩu
            edtMatKhau.setTransformationMethod(PasswordTransformationMethod.getInstance());
            imgEye.setImageResource(R.drawable.eye_hide); // Hình mắt đóng
        } else {
            // Hiển thị mật khẩu
            edtMatKhau.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            imgEye.setImageResource(R.drawable.eye_show); // Hình mắt mở
        }
        isPasswordVisible = !isPasswordVisible;

        // Đưa con trỏ về cuối trường nhập mật khẩu
        edtMatKhau.setSelection(edtMatKhau.getText().length());
    }


    public void Event() {
        listNhanVien = findViewById(R.id.listNhanVien);
        spVaiTro = findViewById(R.id.spNhanVien);
        btnThem = findViewById(R.id.btnThem);
        btnSua = findViewById(R.id.btnSua);
        btnBack = findViewById(R.id.btnBack);
        imgMatTruocCCCD = findViewById(R.id.imgMatTruocCCCD);
        imgMatSauCCCD = findViewById(R.id.imgMatSauCCCD);
        edtTenDangNhap = findViewById(R.id.iEdtEmail);
        edtTenNhanVien = findViewById(R.id.iEdtNhanVien);
        edtMatKhau = findViewById(R.id.iEdtMatKhau);
        edtSoDT = findViewById(R.id.iEdtSDT);
        edtSoCCCD = findViewById(R.id.iEdtCCCD);
        edtNgayCap = findViewById(R.id.iEdtNgayCap);
        imgMatSauCCCD = findViewById(R.id.imgMatSauCCCD);
        imgMatTruocCCCD = findViewById(R.id.imgMatTruocCCCD);
        refreshLayout = findViewById(R.id.swipeRefreshLayout);
        imgEye = findViewById(R.id.imgEye);
    }
}

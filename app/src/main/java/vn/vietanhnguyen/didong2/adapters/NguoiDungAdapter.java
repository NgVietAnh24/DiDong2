package vn.vietanhnguyen.didong2.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import vn.vietanhnguyen.didong2.R;
import vn.vietanhnguyen.didong2.activities.manages.QLNhanVien;
import vn.vietanhnguyen.didong2.models.NguoiDung;

public class NguoiDungAdapter extends RecyclerView.Adapter<NguoiDungAdapter.ViewHolder> {
    List<NguoiDung> list;
    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseStorage storage;

    private OnItemClickListener onItemClickListener;
    public int selectedPosition = -1;

    public interface OnItemClickListener {
        void onItemClick(NguoiDung user);
    }

    public NguoiDungAdapter(List<NguoiDung> list, OnItemClickListener listener) {
        this.list = list;
        this.onItemClickListener = listener;
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nguoi_dung, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NguoiDungAdapter.ViewHolder holder, int position) {
        NguoiDung user = list.get(position);
        holder.tvName.setText(user.getTenNhanVien());
        if (user.getRoles() == 0) {
            holder.tvRole.setText("Quản lý");
        } else if (user.getRoles() == 1) {
            holder.tvRole.setText("Phục vụ");
        }
        holder.tvNgayTao.setText(user.getNgayTao());
        holder.tvNgayCapNhat.setText(user.getNgayCapNhat());
        int pos = position;
        // Kiểm tra trạng thái chọn để cập nhật màu nền
        if (position == selectedPosition) {
            holder.itemLayout.setBackground(ContextCompat.getDrawable(holder.itemLayout.getContext(), R.drawable.item_background_selector));
        } else {
            holder.itemLayout.setBackground(ContextCompat.getDrawable(holder.itemLayout.getContext(), R.drawable.item_background));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int previousPosition = selectedPosition;

                // If the same item is clicked again, deselect it
                if (selectedPosition == pos) {
                    selectedPosition = -1; // Deselect
                    ((QLNhanVien) view.getContext()).resetInputFields();
                    ((QLNhanVien) view.getContext()).btnThem.setAlpha(1f);
                    ((QLNhanVien) view.getContext()).btnThem.setEnabled(true);
                    ((QLNhanVien) view.getContext()).btnSua.setAlpha(0.5f);
                    ((QLNhanVien) view.getContext()).btnSua.setEnabled(false);
                } else {
                    // Select new position and set UI changes accordingly
                    selectedPosition = pos;
                    ((QLNhanVien) view.getContext()).btnThem.setEnabled(false);
                    ((QLNhanVien) view.getContext()).btnThem.setAlpha(0.5f);
                    onItemClickListener.onItemClick(user); // Callback to display user data
                }

                // Update previous and new positions
                if (previousPosition != -1) {
                    notifyItemChanged(previousPosition);
                }
                notifyItemChanged(selectedPosition);
            }
        });


        holder.btnXoa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user.getRoles() != 0) {
                    showDeleteConfirmationDialog(user.getUid(), holder.itemView.getContext());
                    ((QLNhanVien) view.getContext()).resetInputFields();
                } else {
                    Toast.makeText(view.getContext(), "Không được phép xóa quản lý ⚠️", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout itemLayout;
        TextView tvName, tvRole, tvNgayTao, tvNgayCapNhat;
        Button btnXoa;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemLayout = itemView.findViewById(R.id.itemLayout);
            tvName = itemView.findViewById(R.id.tvTen);
            tvRole = itemView.findViewById(R.id.tvVaiTro);
            tvNgayTao = itemView.findViewById(R.id.tvNgayTao);
            tvNgayCapNhat = itemView.findViewById(R.id.tvNgayCapNhat);
            btnXoa = itemView.findViewById(R.id.btnXoa);
        }
    }

    private void showDeleteConfirmationDialog(String uid, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Xóa người dùng");
        builder.setMessage("Bạn có chắc chắn muốn xóa người dùng này?");
        builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteUser(uid, context); // Gọi phương thức xóa và truyền context
            }
        });
        builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Đóng hộp thoại
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteUser(String uid, Context context) {
        user = mAuth.getCurrentUser();

        if (user == null) {
            Log.e("NguoiDungAdapter", "User is not signed in.");
            Toast.makeText(context, "Không có người dùng nào đăng nhập", Toast.LENGTH_SHORT).show();
            return; // Exit the method early if no user is signed in
        }

        firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String frontImageUri = documentSnapshot.getString("matTruocCCCD");
                            String backImageUri = documentSnapshot.getString("matSauCCCD");

                            if (frontImageUri != null) {
                                deleteImageFromFirebase(frontImageUri, "Mặt trước", context);
                            }
                            if (backImageUri != null) {
                                deleteImageFromFirebase(backImageUri, "Mặt sau", context);
                            }

                            firestore.collection("users").document(uid)
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            user.delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            Toast.makeText(context, "Xóa thành công ☑️", Toast.LENGTH_SHORT).show();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.e("NguoiDungAdapter", "Error deleting user from Auth", e);
                                                        }
                                                    });
                                            ((QLNhanVien) context).docDulieu();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context, "Xóa thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Lỗi khi lấy thông tin người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void deleteImageFromFirebase(String imageUri, String imageType, Context context) {
        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUri);

        // Thực hiện việc xóa ảnh
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
//                Toast.makeText(context, "Đã xóa " + imageType + " thành công", Toast.LENGTH_SHORT).show();
                Log.d("image", "Đã xóa " + imageType + " thành công");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
//                Log.e("NguoiDungAdapter", "Error deleting " + imageType + " from Storage", exception);
                Log.d("image", "Đã xóa " + imageType + " thành công");
            }
        });
    }





}

package vn.vietanhnguyen.didong2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import vn.vietanhnguyen.didong2.R;
import vn.vietanhnguyen.didong2.models.DanhMuc;

public class DanhMucAdapter extends ArrayAdapter<DanhMuc> {
    private final Context context; // Đối tượng Context
    private final List<DanhMuc> categories; // Danh sách các danh mục
    private final FirebaseFirestore db; // Tham chiếu đến Firestore

    // Constructor
    public DanhMucAdapter(Context context, List<DanhMuc> categories) {
        super(context, R.layout.item_danh_muc, categories); // Gọi đến constructor của ArrayAdapter
        this.context = context;  // Lưu context
        this.categories = categories;   // Lưu danh sách danh mục
        this.db = FirebaseFirestore.getInstance(); // Khởi tạo Firestore
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Kiểm tra nếu convertView chưa được khởi tạo
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); // Lấy LayoutInflater từ context
            convertView = inflater.inflate(R.layout.item_danh_muc, parent, false);
        }

        // Lấy đối tượng CategoryModel tại vị trí `position`
        DanhMuc category = categories.get(position);

        // Khởi tạo TextView và hiển thị tên danh mục
        TextView textViewCategoryName = convertView.findViewById(R.id.textViewCategoryName);
        textViewCategoryName.setText(category.getName());

        // Thêm sự kiện ấn giữ để xóa danh mục
        convertView.setOnLongClickListener(v -> {
            // Tạo AlertDialog để xác nhận xóa
            new AlertDialog.Builder(context)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa danh mục: " + category.getName() + " không?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        // Xóa danh mục từ Firestore
                        db.collection("categories").document(category.getId())
                                .delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // Nếu xóa thành công từ Firestore
                                            categories.remove(position);
                                            notifyDataSetChanged(); // Cập nhật adapter để hiển thị danh sách mới
                                            Toast.makeText(context, "Đã xóa danh mục: " + category.getName(), Toast.LENGTH_SHORT).show(); // Hiển thị thông báo
                                        } else {
                                            Toast.makeText(context, "Lỗi khi xóa danh mục từ Firestore", Toast.LENGTH_SHORT).show(); // Thông báo lỗi
                                        }
                                    }
                                });
                    })
                    .setNegativeButton("Không", null) // Không làm gì nếu người dùng chọn không
                    .show(); // Hiển thị AlertDialog
            return true; // Trả về true để cho biết sự kiện đã được xử lý
        });

        return convertView;
    }
}
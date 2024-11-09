package vn.vietanhnguyen.didong2.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import vn.vietanhnguyen.didong2.R;
import vn.vietanhnguyen.didong2.activities.manages.QLMonAn;
import vn.vietanhnguyen.didong2.models.MonAn;

public class MonAnAdapter extends RecyclerView.Adapter<MonAnAdapter.FoodViewHolder> {

    private List<MonAn> foodList; // Danh sách các món ăn sẽ hiển thị
    private OnItemClickListener onItemClickListener; // Listener để xử lý sự kiện click
    private Context context; // Context để sử dụng trong Glide

    // Constructor
    public MonAnAdapter(Context context, List<MonAn> foodList) {
        this.context = context; // Lưu lại context
        this.foodList = foodList; // Lưu danh sách món ăn
    }

    // Tạo interface để xử lý sự kiện click
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    // Thêm setter cho listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @Override
    public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Tạo ViewHolder mới bằng cách inflating layout item_food
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mon_an, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FoodViewHolder holder, int position) {
        // Gán dữ liệu cho ViewHolder tại vị trí tương ứng
        MonAn food = foodList.get(position);
        holder.foodName.setText(food.getName());
        holder.foodPrice.setText(food.getPrice() + " VND");  // Có thể định dạng lại giá trị tiền tệ nếu cần

        String imageUrl = food.getImage(); // Lấy URL hình ảnh từ getImage()
        Log.d("FoodAdapter", "Image URL: " + imageUrl);// Ghi log URL hình ảnh

        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Sử dụng Glide để tải ảnh từ URL
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.image_food)  // Hiển thị ảnh mặc định khi đang tải
                    .error(R.drawable.anhloi)   // Hiển thị ảnh lỗi khi không tải được ảnh
                    .into(holder.foodImage); // Đặt hình ảnh vào ImageView
        } else {
            // Đặt hình ảnh mặc định nếu không có URL
            holder.foodImage.setImageResource(R.drawable.image_food);
        }
        int pos = position;
        // Thêm sự kiện click cho từng item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(pos);
                }else {
                    ((QLMonAn) v.getContext()).resetInputFields();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        // Trả về số lượng món ăn trong danh sách
        return foodList.size();
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView foodImage; // ImageView để hiển thị hình ảnh món ăn
        TextView foodName, foodPrice; // TextView để hiển thị tên món ăn và  giá món ăn

        public FoodViewHolder(View itemView) {
            super(itemView);
            foodImage = itemView.findViewById(R.id.imageViewFood);
            foodName = itemView.findViewById(R.id.textViewName);
            foodPrice = itemView.findViewById(R.id.textViewPrice);
        }
    }
}
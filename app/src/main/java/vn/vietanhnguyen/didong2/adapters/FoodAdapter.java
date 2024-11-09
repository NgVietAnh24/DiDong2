package vn.vietanhnguyen.didong2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import vn.vietanhnguyen.didong2.R;
import vn.vietanhnguyen.didong2.models.Food;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {
    private List<Food> foodList;
    private Context context;
    private OnFoodClickListener listener;

    public interface OnFoodClickListener {
        void onFoodClick(Food food);
    }

    public FoodAdapter(List<Food> foodList, Context context, OnFoodClickListener listener) {
        this.foodList = foodList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.food_item, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Food food = foodList.get(position);
        holder.foodName.setText(food.getName());
        // Chuyển đổi và định dạng giá tiền sang VND
        holder.foodPrice.setText(String.format("%,d VNĐ", (long)(food.getPrice() ))); // 23000 là tỷ giá USD/VND

        Glide.with(context)
                .load(food.getImage())
                .placeholder(R.drawable.upload)
                .into(holder.foodImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFoodClick(food);
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView foodImage;
        TextView foodName;
        TextView foodPrice;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImage = itemView.findViewById(R.id.sample_food_image);
            foodName = itemView.findViewById(R.id.food_name);
            foodPrice = itemView.findViewById(R.id.food_price);
        }
    }

    public void updateData(List<Food> newFoodList) {
        this.foodList = newFoodList;
        notifyDataSetChanged();
    }
}

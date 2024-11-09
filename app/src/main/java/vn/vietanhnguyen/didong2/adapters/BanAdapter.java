package vn.vietanhnguyen.didong2.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.vietanhnguyen.didong2.R;
import vn.vietanhnguyen.didong2.models.Ban;


public class BanAdapter extends RecyclerView.Adapter<BanAdapter.ViewHolder> {
    private List<Ban> list;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener{
        void onItemClick(Ban ban);
    }

    public BanAdapter(List<Ban> list, OnItemClickListener onItemClickListener) {
        this.list = list;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onBindViewHolder(@NonNull BanAdapter.ViewHolder holder, int position) {
        Ban ban = list.get(position);
        holder.tvTenBan.setText(ban.getName());
        holder.tvMoTa.setText(ban.getDescription());

        String state = ban.getStatus();
        Log.d("BanAdapter", "State: " + state); // Log để kiểm tra giá trị state

        if (ban.getStatus() != null) {
           if(ban.getStatus().equals("Trống")){
               holder.vState.setBackgroundResource(R.drawable.rounded_trang_thai);
           }else if(ban.getStatus().equals("Đã đặt")){
               holder.vState.setBackgroundResource(R.drawable.rounded_trang_thai_da_dat);
           }else if(ban.getStatus().equals("Đang sử dụng")) {
               holder.vState.setBackgroundResource(R.drawable.rounded_trang_thai_dang_su_dung);
           }

        } else {
            // Xử lý trường hợp state là null
            holder.vState.setBackgroundResource(R.drawable.item_background_selector); // Hoặc một màu khác phù hợp
        }
//        Log.d("ABC","Trạng Thái: "+ban.getStatus());
    }




    @NonNull
    @Override
    public BanAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_table_list, parent,false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenBan, tvMoTa;
        View vState;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenBan = itemView.findViewById(R.id.tvTenBan);
            tvMoTa = itemView.findViewById(R.id.tvMoTa);
            vState = itemView.findViewById(R.id.vState);
        }
    }
}

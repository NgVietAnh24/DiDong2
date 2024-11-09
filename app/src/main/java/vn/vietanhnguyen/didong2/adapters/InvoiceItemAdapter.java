package vn.vietanhnguyen.didong2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import vn.vietanhnguyen.didong2.models.Invoice;
import vn.vietanhnguyen.didong2.R;

public class InvoiceItemAdapter extends ArrayAdapter<Invoice.InvoiceItem> {
    private Context context;
    private int resource;
    private List<Invoice.InvoiceItem> items;

    public InvoiceItemAdapter(@NonNull Context context, int resource, @NonNull List<Invoice.InvoiceItem> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.items = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
        }

        Invoice.InvoiceItem item = getItem(position);

        if (item != null) {
            TextView nameTextView = convertView.findViewById(R.id.itemNameTextView);
            TextView quantityTextView = convertView.findViewById(R.id.itemQuantityTextView);
            TextView priceTextView = convertView.findViewById(R.id.itemPriceTextView);

            nameTextView.setText(item.getName());
            quantityTextView.setText(String.valueOf(item.getQuantity()));
            // Cập nhật định dạng giá tiền thành VND
            priceTextView.setText(String.format("%,.0f VND", item.getPrice() * item.getQuantity())); // Hiển thị tiền Việt Nam
        }

        return convertView;
    }
}
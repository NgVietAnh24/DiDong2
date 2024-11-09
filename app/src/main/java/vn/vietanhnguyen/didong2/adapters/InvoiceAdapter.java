package vn.vietanhnguyen.didong2.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import vn.vietanhnguyen.didong2.activities.InvoiceDetailActivity;
import vn.vietanhnguyen.didong2.R;
import vn.vietanhnguyen.didong2.models.Invoice;

public class InvoiceAdapter extends ArrayAdapter<Invoice> {
    private Context context;
    private int resource;
    private List<Invoice> invoiceList;
    private FirebaseFirestore db;

    public InvoiceAdapter(@NonNull Context context, int resource, @NonNull List<Invoice> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.invoiceList = objects;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
        }

        Invoice invoice = getItem(position);

        if (invoice != null) {
            //TextView hdIdTextView = convertView.findViewById(R.id.hdIdTextView);
            TextView titleTextView = convertView.findViewById(R.id.invoiceTitleTextView);
            TextView timeTextView = convertView.findViewById(R.id.invoiceTimeTextView);
            TextView dateTextView = convertView.findViewById(R.id.invoiceDateTextView);
            TextView tableNameTextView = convertView.findViewById(R.id.tableNameTextView);
            TextView statusTextView = convertView.findViewById(R.id.statusTextView);
            TextView totalTextView = convertView.findViewById(R.id.totalTextView);
            ImageButton deleteButton = convertView.findViewById(R.id.deleteButton);

            //hdIdTextView.setText("#" + invoice.getId());

            titleTextView.setText(invoice.getTitle());
            timeTextView.setText(invoice.getTime());
            dateTextView.setText(invoice.getDate());
            tableNameTextView.setText("Bàn: " + (invoice.getTableName() != null ? invoice.getTableName() : "N/A"));
            statusTextView.setText(invoice.getPaymentStatus());
            // Chỉnh sửa định dạng hiển thị tổng tiền
            totalTextView.setText(String.format("%,.0f VND", invoice.getTotal())); // Hiển thị tiền Việt Nam


            if ("Đã thanh toán".equals(invoice.getPaymentStatus())) {
                statusTextView.setTextColor(context.getResources().getColor(R.color.green));
            } else {
                statusTextView.setTextColor(context.getResources().getColor(R.color.red));
            }

            deleteButton.setOnClickListener(v -> showDeleteConfirmation(invoice));

            // Chuyển sang màn hình InvoiceDetailActivity khi nhấn vào một hóa đơn
            convertView.setOnClickListener(v -> {
                Intent intent = new Intent(context, InvoiceDetailActivity.class);
                intent.putExtra("invoiceId", invoice.getId());  // Truyền ID của hóa đơn
                context.startActivity(intent);
            });
        }

        return convertView;
    }


    private void showDeleteConfirmation(Invoice invoice) {
        new AlertDialog.Builder(context)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa hóa đơn này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteInvoice(invoice))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteInvoice(Invoice invoice) {
        db.collection("invoices")
                .document(invoice.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    invoiceList.remove(invoice);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Đã xóa hóa đơn", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Lỗi khi xóa hóa đơn", Toast.LENGTH_SHORT).show();
                });
    }
}
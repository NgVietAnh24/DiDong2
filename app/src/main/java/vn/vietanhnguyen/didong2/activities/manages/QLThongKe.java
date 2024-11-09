package vn.vietanhnguyen.didong2.activities.manages;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import vn.vietanhnguyen.didong2.HomeQuanLy;
import vn.vietanhnguyen.didong2.R;
import vn.vietanhnguyen.didong2.models.ThongKe;

public class QLThongKe extends AppCompatActivity {
    private TextView tvTongTien;
    private ImageButton btnBack;
    private Spinner spinYear;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<String> yearList;
    private ArrayAdapter<String> adapterYear;
    private LineChart lineChartDoanhThu;
    private List<ThongKe> doanhThuList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_ke);
        connectXML();
        doanhThuList = new ArrayList<>();
        yearList = new ArrayList<>();
        // ghiDuLieu();
        ghiDuLieuTuInVoices();
        docNamTuFireStore();

        spinYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedYear = yearList.get(i);
                docDoanhThu(selectedYear);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(QLThongKe.this, HomeQuanLy.class);
                startActivity(intent);
            }
        });

    }
    private void connectXML() {
        tvTongTien = findViewById(R.id.tvTongTien);
        spinYear = findViewById(R.id.spinYear);
        btnBack = findViewById(R.id.btnBack);
        lineChartDoanhThu = findViewById(R.id.lineChartDoanhThu);
        lineChartDoanhThu.setDragEnabled(true);
        lineChartDoanhThu.setScaleEnabled(false);
    }
/*
    //    Ham ghi du lieu
    private void ghiDuLieu() {
        Random random = new Random();
        for (int year = 2022; year <= 2024; year++) {
            for (int month = 1; month <= 12; month++) {
                int monthlyIncome = random.nextInt(10000000) + 100;
                // Tạo đối tượng ThongKeModels
                ThongKeModels thongKe = new ThongKeModels("tk_" + year + "_" + month, // ID thống kê
                        monthlyIncome,
                        year,
                        month,
                        "hd_" + year + "_" + month
                );
                // Ghi dữ liệu vào Firestore
                db.collection("thongke").document(thongKe.getTk_id()).set(createDataMap(thongKe)).addOnSuccessListener(aVoid -> {
                    Log.d("SSS", "Thanh cong");
                }).addOnFailureListener(e -> {

                    Log.d("SSS", "That bai");

                });
            }
        }
    }

    // Tao datamap cho thongkemodels
    @NonNull
    private Map<String, Object> createDataMap(ThongKeModels thongKe) {
        Map<String, Object> data = new HashMap<>();
        data.put("tk_id", thongKe.getTk_id());
        //  data.put("tong_tien_nam", thongKe.getTong_tien_nam());
        data.put("tong_tien_thang", thongKe.getTong_tien_thang());
        data.put("nam", thongKe.getNam());
        data.put("thang", thongKe.getThang());
        data.put("hd_id", thongKe.getHd_id());
        return data;
    }

 */

    //    Doc du lieu nam de truyen vao spinner
    private void docNamTuFireStore() {
        db.collection("ThongKe").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Lấy năm từ tài liệu
                    int year = document.getLong("nam").intValue();
                    if (!yearList.contains(String.valueOf(year))) {
                        yearList.add(String.valueOf(year)); // Thêm năm vào danh sách
                    }
                }
                adapterYear = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, yearList);
                adapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinYear.setAdapter(adapterYear);
            }
        });
    }
    //Doc du lieu doanh thu
    private void docDoanhThu(String year) {
        doanhThuList.clear(); // Làm sạch danh sách doanh thu trước khi lấy dữ liệu mới
        db.collection("ThongKe").whereEqualTo("nam", Integer.parseInt(year))
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        double tongTienNam = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ThongKe thongKe = document.toObject(ThongKe.class);
                            doanhThuList.add(thongKe);
                            tongTienNam += thongKe.getTong_tien_thang();
                        }
                        updateLineChart(); // Cập nhật biểu đồ
                        DecimalFormat deci = new DecimalFormat("#,###");
                        String tongTienDeciMal = deci.format(tongTienNam);
                        tvTongTien.setText("Tổng tiền trong năm: " + tongTienDeciMal);
                    } else {
                        Toast.makeText(this, "Error getting documents: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateLineChart() {
        float[] doanhThuTheoThang = new float[12];//mang luu tru 12 thang
        for (ThongKe thongKe : doanhThuList) {
            int thang = thongKe.getThang() - 1;
            if (thang >= 0 && thang < 12) {
                doanhThuTheoThang[thang] = thongKe.getTong_tien_thang();
            }
        }
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            entries.add(new Entry(i + 1, doanhThuTheoThang[i]));
        }
        LineDataSet dataSet = new LineDataSet(entries, "Doanh thu theo tháng");
        dataSet.setFillAlpha(110);
        dataSet.setLineWidth(3.5f);
        dataSet.setColor(Color.RED);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(Color.RED);
        dataSet.setValueTextSize(10f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.2f);

        LineData lineData = new LineData(dataSet);
        lineChartDoanhThu.setData(lineData);
        lineChartDoanhThu.getDescription().setEnabled(false);
//Cap nhat gia tri trục Y
        YAxis yAxis = lineChartDoanhThu.getAxisLeft();
        yAxis.setAxisMinimum(10000);
        yAxis.setAxisMaximum(10000000);
        yAxis.setTextColor(Color.RED);
//Cap nhat gia tri truc X
        XAxis xAxis = lineChartDoanhThu.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.RED);
//        Hien thi day du 12 thang
        xAxis.setLabelCount(12, true);
        String[] months = {"T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"};
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int index = (int) value - 1;
                if (index >= 0 && index < months.length) {
                    return months[index];
                } else {
                    return "";
                }
            }
        });
        lineChartDoanhThu.getAxisRight().setEnabled(false);//Tat truc y ben phai
        lineChartDoanhThu.invalidate();
    }
    //    Ham doc du lieu tu invoices va ghi vao ThongKe
    private void ghiDuLieuTuInVoices() {
        db.collection("invoices").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, Double> monthlyTotals = new HashMap<>();
                Map<String, List<String>> monthlyHdIds = new HashMap<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    double tongTien = document.getDouble("tong_tien");
                    String hdId = document.getId();
                    Date ngayTaoDate = null;
                    if (document.contains("ngay_tao")) {
                        String ngayTaoString = document.getString("ngay_tao");

                        if (ngayTaoString != null) {
                            try {
                                // Giả sử ngày được lưu dưới dạng "dd/MM/yyyy"
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                ngayTaoDate = sdf.parse(ngayTaoString);
                            } catch (Exception e) {
                                Log.w("Firestore", "Lỗi khi chuyển đổi ngày: " + ngayTaoString, e);
                            }
                        }
                    }
                    // Nếu có ngày tạo hợp lệ, tiếp tục xử lý
                    if (ngayTaoDate != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(ngayTaoDate);
                        int thang = cal.get(Calendar.MONTH) + 1;
                        int nam = cal.get(Calendar.YEAR);
                        String monthKey = nam + "_" + thang;
                        monthlyTotals.put(monthKey, monthlyTotals.getOrDefault(monthKey, 0.0) + tongTien);
                        monthlyHdIds.putIfAbsent(monthKey, new ArrayList<>());
                        monthlyHdIds.get(monthKey).add(hdId);
                    } else {
                        Log.w("Firestore", "Trường 'ngay_tao' không hợp lệ trong tài liệu HoaDon với ID: " + hdId);
                    }
                }
                // Tạo thống kê cho từng tháng
                for (Map.Entry<String, Double> entry : monthlyTotals.entrySet()) {
                    String[] parts = entry.getKey().split("_");
                    int nam = Integer.parseInt(parts[0]);
                    int thang = Integer.parseInt(parts[1]);
                    double tongTienThang = entry.getValue();
                    List<String> hdIds = monthlyHdIds.get(entry.getKey());

                    // Tạo đối tượng ThongKeModels
                    ThongKe thongKe = new ThongKe("tk_" + nam + "_" + thang,
                            (int) tongTienThang,
                            nam,
                            thang,
                            hdIds
                    );
                    // Ghi dữ liệu vào Firestore
                    db.collection("ThongKe").document(thongKe.getTk_id()).set(createDataMapThongKe(thongKe)).addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Thêm dữ liệu thành công cho: " + thongKe.getTk_id());
                    }).addOnFailureListener(e -> {
                        Log.d("Firestore", "Thêm dữ liệu thất bại", e);
                    });
                }
            } else {
                Log.w("Firestore", "Lỗi khi lấy dữ liệu từ HoaDon.", task.getException());
            }
        });
    }
    @NonNull
    private Map<String, Object> createDataMapThongKe(ThongKe thongKe) {
        Map<String, Object> data = new HashMap<>();
        data.put("tk_id", thongKe.getTk_id());
        data.put("tong_tien_thang", thongKe.getTong_tien_thang());
        data.put("nam", thongKe.getNam());
        data.put("thang", thongKe.getThang());
        data.put("hd_id", thongKe.getHd_id());
        return data;
    }

}
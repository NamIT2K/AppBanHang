package com.example.nammobile.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.icu.text.CompactDecimalFormat;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.example.nammobile.R;
import com.example.nammobile.adapter.LoaiSpAdapter;
import com.example.nammobile.adapter.SanPhamMoiAdapter;
import com.example.nammobile.model.GioHang;
import com.example.nammobile.model.LoaiSp;
import com.example.nammobile.model.LoaiSpModel;
import com.example.nammobile.model.SanPhamMoi;
import com.example.nammobile.model.SanPhamMoiModel;
import com.example.nammobile.model.User;
import com.example.nammobile.retrofit.ApiBanHang;
import com.example.nammobile.retrofit.RetrofitClient;
import com.example.nammobile.utils.Utils;
import com.google.android.material.navigation.NavigationView;
import com.nex3z.notificationbadge.NotificationBadge;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.internal.schedulers.SchedulerWhen;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;
    ViewFlipper viewFlipper;
    RecyclerView recyclerViewManHinhChinh;
    NavigationView navigationView;
    ListView listViewManHinhChinh;
    DrawerLayout drawerLayout;
    LoaiSpAdapter loaiSpAdapter;
    List<LoaiSp> mangloaisp;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    ApiBanHang apiBanHang;
    List<SanPhamMoi> mangSpMoi;
    SanPhamMoiAdapter spAdapter;
    NotificationBadge badge;
    FrameLayout frameLayout;
    ImageView imgsearch;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        apiBanHang = RetrofitClient.getInstance(Utils.BASE_URL).create(ApiBanHang.class);
        Paper.init(this);
        if (Paper.book().read("user") != null){
            User user = Paper.book().read("user");
            Utils.user_current = user;
        }
        Anhxa();
        ActionBar();

        if (isConnected(this)){
//            Toast.makeText(getApplicationContext(), "Đã kết nối", Toast.LENGTH_LONG).show();
            ActionViewFlipper();
            getLoaiSanPham();
            getSpMoi();
            getEventClick();
        }else {
            Toast.makeText(getApplicationContext(), "Không có Internet, vui lòng kết nối", Toast.LENGTH_LONG).show();
        }
    }

    private void getEventClick() {
        listViewManHinhChinh.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        Intent trangchu = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(trangchu);
                        break;
                    case 1:
                        Intent dienthoai = new Intent(getApplicationContext(), DienThoaiActivity.class);
                        dienthoai.putExtra("hang",1);
                        startActivity(dienthoai);
                        break;
                    case 2:
                        Intent samsung = new Intent(getApplicationContext(), DienThoaiActivity.class);
                        samsung.putExtra("hang",2);
                        startActivity(samsung);
                        break;
                    case 3:
                        Intent oppo = new Intent(getApplicationContext(), DienThoaiActivity.class);
                        oppo.putExtra("hang",3);
                        startActivity(oppo);
                        break;
                    case 4:
                        Intent vivo = new Intent(getApplicationContext(), DienThoaiActivity.class);
                        vivo.putExtra("hang",4);
                        startActivity(vivo);
                        break;
                    case 5:
                        Intent xiaomi = new Intent(getApplicationContext(), DienThoaiActivity.class);
                        xiaomi.putExtra("hang",5);
                        startActivity(xiaomi);
                        break;
                    case 6:
                        Intent phukien = new Intent(getApplicationContext(), DienThoaiActivity.class);
                        phukien.putExtra("hang",6);
                        startActivity(phukien);
                        break;
                    case 9:
                        Intent donhang = new Intent(getApplicationContext(), LichSuDonHangActivity.class);
                        startActivity(donhang);
                        break;
                    case 10:
                        // Xóa key user
                        Paper.book().delete("user");
                        Intent dangnhap = new Intent(getApplicationContext(), DangNhapActivity.class);
                        startActivity(dangnhap);
                        finish();
                        break;
                }
            }
        });
    }

    private void getSpMoi() {
        compositeDisposable.add(apiBanHang.getSpMoi()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
                sanPhamMoiModel -> {
                    if (sanPhamMoiModel.isSuccess()){
                        mangSpMoi = sanPhamMoiModel.getResult();
                        spAdapter = new SanPhamMoiAdapter(getApplicationContext(), mangSpMoi);
                        recyclerViewManHinhChinh.setAdapter(spAdapter);

                    }

                },
                throwable -> {
                    Toast.makeText(getApplicationContext(), "Không kết nối được với sever" +throwable.getMessage(),Toast.LENGTH_LONG).show();
                }
        ));
    }

    private void getLoaiSanPham() {
        compositeDisposable.add(apiBanHang.getLoaiSp()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
                loaiSpModel -> {
                    if (loaiSpModel.isSuccess()){
                        mangloaisp = loaiSpModel.getResult();
                        mangloaisp.add(new LoaiSp("Đăng xuất",""));
                        // khoi tao adapter
                        loaiSpAdapter = new LoaiSpAdapter(getApplicationContext() ,mangloaisp);
                        listViewManHinhChinh.setAdapter(loaiSpAdapter);
                    }

                },
                throwable -> {
                    Log.d("t", throwable.getMessage());
                }
        ));

    }

    private void ActionViewFlipper() {
        List<String> quangcao = new ArrayList<>();
        quangcao.add("https://image.thanhnien.vn/768/uploaded/nthanhluan/2021_06_29/1466_vfqn.jpg");//cấp phát vùng bộ nhớ
        quangcao.add("https://anphat.com.vn/media/lib/22-09-2021/iphone-13-pro-va-iphone-13-pro-max-co-dang-de-cho-doi-1.png");
        quangcao.add("https://baoquocte.vn/stores/news_dataimages/trungkhu/082021/12/11/1417_adadadsadasdasda.jpg?rt=20210812111418");
        quangcao.add("https://images.fpt.shop/unsafe/fit-in/585x390/filters:quality(90):fill(white)/fptshop.com.vn/Uploads/Originals/2021/7/8/637613323642518520_oppo-reno6z-xanh-1.jpg");
        quangcao.add("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRt1gAIcq45lng1_bKePNLKdPAQksSGfOOPAAMNhYbcn62I0pMLH5WqUpQYb87YJij8aIo&usqp=CAU");
        quangcao.add("https://fptshop.com.vn/Uploads/images/2015/Tin-Tuc/QuanLNH2/vivo-y21-1.jpg");
        for (int i = 0; i<quangcao.size(); i++){
            ImageView imageView = new ImageView(getApplicationContext());
            Glide.with(getApplicationContext()).load(quangcao.get(i)).into(imageView);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            viewFlipper.addView(imageView);
        }
        viewFlipper.setFlipInterval(3000);
        viewFlipper.setAutoStart(true);
        Animation slide_in = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_right);
        Animation slide_out = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_right);
        viewFlipper.setInAnimation(slide_in);
        viewFlipper.setOutAnimation(slide_out);

    }

    private void ActionBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_sort_by_size);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void Anhxa() {
        imgsearch = findViewById(R.id.imgsearch);
        toolbar = findViewById(R.id.toolbarmanhinhchinh);
        viewFlipper = findViewById(R.id.viewlipper);
        recyclerViewManHinhChinh = findViewById(R.id.recyclerview);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerViewManHinhChinh.setLayoutManager(layoutManager);
        recyclerViewManHinhChinh.setHasFixedSize(true);
        listViewManHinhChinh = findViewById(R.id.listviewmanhinhchinh);
        navigationView = findViewById(R.id.navigationview);
        drawerLayout = findViewById(R.id.drawerlayout);
        badge = findViewById(R.id.menu_sl);
        frameLayout = findViewById(R.id.framegiohang);
        // khoi tao list
        mangloaisp = new ArrayList<>();
        mangSpMoi = new ArrayList<>();
        if (Utils.manggiohang == null){
            Utils.manggiohang = new ArrayList<>();
        }else {
            int totalItem = 0;
            for (int i=0; i<Utils.manggiohang.size(); i++){
                totalItem = totalItem + Utils.manggiohang.get(i).getSoluong();
            }
            badge.setText(String.valueOf(totalItem));
        }
        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent giohang = new Intent(getApplicationContext(), GioHangActivity.class);
                startActivity(giohang);
            }
        });

        imgsearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), TimKiemActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        int totalItem = 0;
        for (int i=0; i<Utils.manggiohang.size(); i++){
            totalItem = totalItem + Utils.manggiohang.get(i).getSoluong();
        }
        badge.setText(String.valueOf(totalItem));
    }

    private boolean isConnected (Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI); // nho them quyen vao khong bi loi
        NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if ((wifi != null && wifi.isConnected())  ||(mobile != null && mobile.isConnected()) ){
            return true;
        }else {
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}
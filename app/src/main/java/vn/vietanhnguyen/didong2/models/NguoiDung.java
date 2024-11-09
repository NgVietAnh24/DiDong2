package vn.vietanhnguyen.didong2.models;

import java.text.DateFormat;

public class NguoiDung {
    private String uid;
    private String tenDangNhap;
    private String tenNhanVien;
    private String sDT;
    private String soCCCD;
    private String NgayCap;
    private String matTruocCCCD, matSauCCCD;
    private int roles;
    private String ngayTao, ngayCapNhat;

    public NguoiDung() {
    }

    public NguoiDung(String tenDangNhap, String tenNhanVien, String sDT, String soCCCD, String ngayCap, int roles, String ngayTao, String ngayCapNhat) {
        this.tenDangNhap = tenDangNhap;
        this.tenNhanVien = tenNhanVien;
        this.sDT = sDT;
        this.soCCCD = soCCCD;
        NgayCap = ngayCap;
        this.roles = roles;
        this.ngayTao = ngayTao;
        this.ngayCapNhat = ngayCapNhat;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getTenNhanVien() {
        return tenNhanVien;
    }

    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
    }

    public String getsDT() {
        return sDT;
    }

    public void setsDT(String sDT) {
        this.sDT = sDT;
    }

    public String getSoCCCD() {
        return soCCCD;
    }

    public void setSoCCCD(String soCCCD) {
        this.soCCCD = soCCCD;
    }

    public String getNgayCap() {
        return NgayCap;
    }

    public void setNgayCap(String ngayCap) {
        NgayCap = ngayCap;
    }

    public String getMatTruocCCCD() {
        return matTruocCCCD;
    }

    public void setMatTruocCCCD(String matTruocCCCD) {
        this.matTruocCCCD = matTruocCCCD;
    }

    public String getMatSauCCCD() {
        return matSauCCCD;
    }

    public void setMatSauCCCD(String matSauCCCD) {
        this.matSauCCCD = matSauCCCD;
    }

    public int getRoles() {
        return roles;
    }

    public void setRoles(int roles) {
        this.roles = roles;
    }

    public String getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(String ngayTao) {
        this.ngayTao = ngayTao;
    }

    public String getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(String ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }

    public String getVaiTro() {
        switch (roles) {
            case 0:
                return "Quản lý";
            case 1:
                return "Phục vụ";
            default:
                return "Không xác định";
        }
    }
}
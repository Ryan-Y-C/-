package com.wechatshop.entity;

import java.util.List;

public class PageResponse<T> {
    private int pageNum;
    private int pageSize;
    private int totalPage;
    private List<T> data;

    public PageResponse() {
    }

    public static <T> PageResponse<T> of(int id, int pageNum, int pageSize) {
        PageResponse<T> pageResponse = new PageResponse<>();
        pageResponse.setPageNum(pageNum);
        pageResponse.setPageSize(pageSize);
        return pageResponse;

    }

    public static <T> PageResponse<T> pageData(int pageNum, int pageSize, int totalPage, List<T> data) {
        PageResponse<T> pageResponse = new PageResponse<>();
        pageResponse.setPageNum(pageNum);
        pageResponse.setPageSize(pageSize);
        pageResponse.setTotalPage(totalPage);
        pageResponse.setData(data);
        return pageResponse;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}

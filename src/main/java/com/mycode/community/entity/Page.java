package com.mycode.community.entity;

/**
 * 封装分页相关的信息
 */
public class Page {

    //current和limit是页面需要回传给我的数据，rows和path是自己查询设置好的数据，传给页面的
    // 当前的页码
    private int current = 1;
    // 显示的上限
    private int limit = 10;
    // 数据的总数(用于计算总页数)
    private int rows;
    //查询路径(用于复用分页链接)
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current >= 1) {
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    // 当总行数大于1000时，按1000来算
    public void setRows(int rows) {
        if (rows >= 0) {
            if (rows > 1000) {
                this.rows = 1000;
            } else {
                this.rows = rows;
            }
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取当前页的起始行
     * @return offset
     */
    public int getOffset () {
        // current * limit - limit
        return (current - 1) * limit;
    }

    /**
     * 获取总页数
     * @return
     */
    public int getTotal () {
        // rows / limit [+1]
        if (rows % limit == 0) {
            return rows / limit;
        }
        else {
            return rows / limit + 1;
        }
    }

    /**
     * 获取起始页码
     * @return
     */
    public int getFrom () {
        int from = current - 2;
        return from < 1 ? 1: from;
    }

    /**
     * 获取结束页码
     * @return
     */
    public int getEnd () {
        int end = current + 2;
        int total = getTotal();
        return end > total ? total : end;
    }
}

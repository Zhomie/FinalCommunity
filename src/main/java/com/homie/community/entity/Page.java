package com.homie.community.entity;
/**
 * 封装分页相关信息
 * */
public class Page {
    //当前页码
    private int current=1;
    //显示上限
    private  int limit=10;
    //显示总数,用来计算总页数
    private  int rows;
    //路径，用于复用分页链接
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if(current>=1){
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if(limit>=1&&limit<100) {
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if(rows>=0){
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    /**
     *获取当前页的起始行(数据库里的行数)
     *
     */
    public int getOffset(){
        /*通过当前页计算*/
        /*     current*limit-limit*/
        return (current-1)*limit;
    }
    /**
     * 获取总页数
     *
     * */

    public int getTotal(){
        /*不一定整除，多出来的还需要一页*/
        if(rows%limit==0){
            return  rows / limit;
        }
        return rows / limit+1;
    }

    /**
     *  获取显示的起始页码
     * */
    public int getFrom(){
        int from = current-2;

        return from>1 ? from:1;
    }
    /**
     *  获取显示的结束页码
     * */
    public int getTo(){
        int to = current+2;
        int total = getTotal();
        if(to>total){
            return total;
        }
        return to;

        /*上述代码可以简化
         *   return to>total? total:to;
         * */
    }
}

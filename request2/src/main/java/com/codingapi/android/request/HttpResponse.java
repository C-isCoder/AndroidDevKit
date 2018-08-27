package com.codingapi.android.request;

/**
 * Created by iCong.
 * Time:2016/6/21-14:46.
 */
public class HttpResponse<T> {

    private ResultData<T> data;
    private int state;
    private String msg;

    public ResultData<T> getData() {
        return data;
    }

    public String getMsg() {
        return msg;
    }

    public int getState() {
        return state;
    }

    public static class ResultData<T> {
        private int code;
        private String msg;
        private T data;

        public String getMsg() {
            return msg;
        }

        public T getData() {
            return data;
        }

        public int getCode() {
            return code;
        }

        @Override
        public String toString() {
            return "\n"
                + "   [code: "
                + code
                + "\n"
                + "     msg: "
                + msg
                + "\n"
                + "    data: "
                + data
                + "\n";
        }
    }

    @Override
    public String toString() {
        return "服务器返回信息:"
            + "\nmsg:"
            + msg
            + "\ndata:"
            + data.toString()
            + "\nstate:"
            + state;
    }
}

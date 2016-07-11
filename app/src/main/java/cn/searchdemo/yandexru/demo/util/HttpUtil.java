package cn.searchdemo.yandexru.demo.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/7.
 */
public class HttpUtil {

    //创建一个Volley的访问队列
    public static RequestQueue queue;

    public RequestQueue getmRequestQueue(final Context context) {
        if (queue == null) {
            queue = Volley.newRequestQueue(context);
            return queue;
        }
        return null;
    }

    public static void getHot(final Context context,String name,final OnGetHotListener listener){
        try{
            //1)判断有没有队列
             if(queue==null) {
                queue = Volley.newRequestQueue(context);
            }

            //2)创建网络请求
            //对关键字进行UTF8转码
            String HotText = URLEncoder.encode(name,"utf8");
            //利用转码后的关键字，拼接URL
           final String url = UrlContants.YANDEX_RU_API_PREFIX +"callback=jQuery214035190920018108574_1467879404434"+"&"+"srv=morda_ru_desktop&wiz=TrWth&lr=21431&uil=ru&fact=1&v=4&icon=1&hl=1&html=1&bemjson=1&yu=344254941467857979&pos=1&"+"part=" +
                    HotText;
                    //+ "&_="+"1467879404440";  // 据说是时间戳
            final StringRequest request = new StringRequest(url, new Response.Listener<String>() {
                @Override
                public void onResponse(String s) {
                    //网络访问成功后，会调用该方法
                    //onResponse方法是在主线程上调用的
                    Log.i("请求是：",url);
                    String text = s.substring(s.indexOf("(")+1,s.lastIndexOf(")"));
                    Log.i("回调是：",text);

                    try {
                        JSONArray array = new JSONArray(text);
                        String arr = array.get(1).toString();
                        JSONArray result = new JSONArray(arr);
                        List<String> lists = new ArrayList<String>();
                        for(int i=0;i<result.length();i++)
                        {
                            String s1 = result.get(i).toString();
                            JSONArray array1 = new JSONArray(s1);
                            String hotword = array1.get(1).toString();
                            Log.i("联想个数为：",""+result.length());
                            Log.i("热点词",""+hotword);
                            lists.add(hotword);
                        }
                        Gson gson = new Gson();
                        listener.onSuccess(lists);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Toast.makeText(context,"网络繁忙，稍后重试",Toast.LENGTH_LONG).show();
                }
            });
            queue.add(request);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //3)把网络访问请求对象放入队列中

    }

    public interface OnGetHotListener{
        void onSuccess(List<String> words);
    }

}

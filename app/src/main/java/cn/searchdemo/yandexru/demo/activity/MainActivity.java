package cn.searchdemo.yandexru.demo.activity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import cn.searchdemo.yandexru.R;
import cn.searchdemo.yandexru.demo.dal.SearchViewListener;
import cn.searchdemo.yandexru.demo.util.HttpUtil;

public class MainActivity extends AppCompatActivity {

    private TextView searchInfo;
    private SearchView searchView;
    private ListView lvWords;

    private MyHandler handler;

    private ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(10);;
    private String currentSearchTip;


    private InputMethodManager inputMethodManager;

    /**
     * 提示adapter （推荐adapter）
     */
    private ArrayAdapter<String> mHintAdapter;

    /**
     * 自动补全adapter 只显示名字
     */
    private ArrayAdapter<String> mAutoCompleteAdapter;
    /**
     * 搜索回调接口
     */
    private SearchViewListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_second);
        initView();
        setSearchView();
        initListener();

    }

    private void initView(){
        searchView = (SearchView) this.findViewById(R.id.search_sv_input);
        searchInfo = (TextView) this.findViewById(R.id.search_tv_info);
        lvWords = (ListView) this.findViewById(R.id.search_lv_tips);

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        handler = new MyHandler();

        //初始化键盘显示
        //show keyboard
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE|
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void setSearchView(){
        searchView.setVisibility(View.VISIBLE);
        searchView.setIconifiedByDefault(true);
        searchView.setIconified(false);
    }

    private void initListener(){

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchInfo.setText("search submit result");
                hideSoftInput();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(s!=null && s.length()>0){
                    currentSearchTip = s;
                    showSearchTip(s);
                }
                return true;
            }
        });
        lvWords.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String text = lvWords.getAdapter().getItem(i).toString();

            }
        });
    }

    public void showSearchTip(String newText){
        schedule(new SearchTipThread(newText),500);
    }

    class SearchTipThread implements Runnable{

        String newText;

        public SearchTipThread(String newText){
            this.newText = newText;
        }

        @Override
        public void run() {
           if(newText != null && newText.equals(currentSearchTip)){
               handler.sendMessage(handler.obtainMessage(1,newText+"search tip"));
           }
        }
    }

    public ScheduledFuture<?> schedule(Runnable command,long delayTimeMills){
        return scheduledExecutor.schedule(command,delayTimeMills, TimeUnit.MILLISECONDS);
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case 1:
                    searchInfo.setText((String) msg.obj);
                    break;
            }
        }
    }

    /**
     * 设置搜索回调接口
     *
     * @param mlistener 监听者
     */
    private void setSearchViewListener(SearchViewListener mlistener){
        this.mListener = mlistener;
    }


    /**
     * hide soft input
     */
    private void hideSoftInput(){
        if(inputMethodManager != null)
        {
            View v = MainActivity.this.getCurrentFocus();
            if(v==null)
            {
                return;
            }
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
            searchView.clearFocus();
        }
    }
}

package cn.searchdemo.yandexru.demo.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.searchdemo.yandexru.R;
import cn.searchdemo.yandexru.demo.util.HttpUtil;
import cn.searchdemo.yandexru.demo.util.UrlContants;
import cn.searchdemo.yandexru.demo.view.SearchView;

/**
 * Created by Administrator on 2016/7/7.
 */
public class CustomActivity extends AppCompatActivity implements SearchView.SearchViewListener, View.OnClickListener {

    private SearchView searchView;
    private LinearLayout mLlHistory;
    private TextView mTvHistoryTitle;
    private ListView mLvHistoryText;
    private Button mBtnHistoryClear;



    private ArrayAdapter<String> hintAdapter;
    private ArrayAdapter<String> autoCompleteAdapter;

    public static final String EXTRA_KEY_TYPE = "extra_key_type";
    public static final String EXTRA_KEY_KEYWORD = "extra_key_keyword";
    public static final String KEY_SEARCH_HISTORY_KEYWORD = "key_search_history_keyword";
    private String mType;
    private SharedPreferences mPref;
    private SharedPreferences.Editor editor;
    private List<String> mHistoryKeywords;

    /*
     *历史记录信息Adapter
     */
    private ArrayAdapter<String> mHistoryAdapter;
    /*
     *总数据
     */
    private List<String> resultData;

    /*
     *网络版数据
     */
    private List<String> hintData;

    /*
     *搜索过程中自动补全数据
     */
    private List<String> autoCompleteData;

    /**
     * 默认提示框显示项的个数
     */
    private static int DEFAULT_HINT_SIZE = 5;

    /*
     *提示框显示项的个数
     */
    private static int hintSize = DEFAULT_HINT_SIZE;

    /**
     * 设置提示框显示项的个数
     *
     * @param hintSize 提示框显示个数
     */
    public static void setHintSize(int hintSize) {
        CustomActivity.hintSize = hintSize;
    }



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取搜索记录文件内容
        mPref = this.getSharedPreferences("search_history.txt", Context.MODE_PRIVATE);
        editor = mPref.edit();

        mType = getIntent().getStringExtra(EXTRA_KEY_TYPE);
        String keyword = getIntent().getStringExtra(EXTRA_KEY_KEYWORD);
        if(!TextUtils.isEmpty(keyword)){
            searchView.setEtInput(keyword);
        }
        mHistoryKeywords = new ArrayList<String>();
        String history = mPref.getString(KEY_SEARCH_HISTORY_KEYWORD,"");
        Log.e("HISTORY",history);
        //判断SharePrefence中的键值对里存储的值不为空时
        if(!TextUtils.isEmpty(history)){
            //合成数组一个用于初始化显示的数组
            List<String> listhistory = new ArrayList<String>();
            for(Object o : history.split(",")){
                listhistory.add((String) o);
            }
            mHistoryKeywords = listhistory;
        }
        initData();
        initView();
        initListener();
        initSearchHistory();

    }



    private void initView() {
        searchView = (SearchView) findViewById(R.id.yadexru_vw_search);
        mLlHistory = (LinearLayout) findViewById(R.id.search_ll_history);
        mTvHistoryTitle = (TextView) findViewById(R.id.search_tv_history);
        mLvHistoryText = (ListView) findViewById(R.id.search_lv_tipshistory);
        mBtnHistoryClear = (Button) findViewById(R.id.search_btn_deletehistory);
        //设置adapter
        searchView.setTipsHintAdapter(hintAdapter);
        searchView.setAutoCompleteAdapter(autoCompleteAdapter);
    }

    private void initListener(){
        //设置监听
        searchView.setSearchViewListener(this);
        mBtnHistoryClear.setOnClickListener(this);
    }

    private void initData() {
        //初始化热搜版数据
        getHintData();
        //初始化自动补全数据
        getAutoCompleteData(null);
        //初始化搜索结果数据
        getResultData(null);
    }

    private void initSearchHistory(){
          //当该数组里的元素大于0时，历史记录LinearLayout显示，否则不予显示
          if(mHistoryKeywords.size()>0) {
              mLlHistory.setVisibility(View.VISIBLE);
          }else{
              mLlHistory.setVisibility(View.GONE);
          }
          mHistoryAdapter = new ArrayAdapter<String>(CustomActivity.this,R.layout.item_search_history,mHistoryKeywords);

          mLvHistoryText.setAdapter(mHistoryAdapter);
          mLvHistoryText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //选择历史记录ListView中的任意一个item后，显示在SearchView中的输入框
                searchView.setEtInput(mHistoryKeywords.get(i));
                //隐藏历史记录LinearLayout mLiHistory
                mLlHistory.setVisibility(View.GONE);
            }
          });
          mHistoryAdapter.notifyDataSetChanged();
    }

    /**
     * 获取data和adapter
     */
    private void getHintData(){
        Log.i("mHistory",""+mHistoryKeywords.size());
        if(mHistoryKeywords.size()!=0) {
            hintAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mHistoryKeywords);
        }else{
            hintAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 0);
        }
    }

    /**
     * 获取
     * @param text
     */
    private void getAutoCompleteData(String text){
        if(autoCompleteData==null){
            //初始化
            autoCompleteData = new ArrayList<>(hintSize);
        }else {
            //根据text 获取auto data
            autoCompleteAdapter.clear();
            if(mHistoryKeywords.size()!=0){
                for(int i=0,count=0;i<mHistoryKeywords.size()&&count<hintSize;i++)
                {
                    if(mHistoryKeywords.get(i).contains(text))
                    {
                        autoCompleteData.add(mHistoryKeywords.get(i));

                        count++;
                    }
                }
            }
        }

        if(autoCompleteAdapter==null){
            autoCompleteAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,autoCompleteData);
        }else{
            autoCompleteAdapter.notifyDataSetChanged();
        }
    }

    private void getResultData(String text){
        if(resultData == null){
            resultData = new ArrayList<>();
        }else{
            resultData.clear();
            for(int i = 0; i < mHistoryKeywords.size();i++){
                if(mHistoryKeywords.get(i).contains(text.trim()))
                {
                    resultData.add(mHistoryKeywords.get(i));
                }
            }
        }
        if(mHistoryAdapter == null){
            mHistoryAdapter = new ArrayAdapter<String>(CustomActivity.this,R.layout.item_search_history,resultData);
        }else{
            mHistoryAdapter.notifyDataSetChanged();
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String type = intent.getStringExtra(EXTRA_KEY_TYPE);
        String keyword = intent.getStringExtra(EXTRA_KEY_KEYWORD);
        if(!TextUtils.isEmpty(keyword)){
            searchView.setEtInput(keyword);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }


    @Override
    public void onRefreshAutoComplete(String text) {
             getAutoCompleteData(text);
    }

    @Override
    public void onSearch(String text) {
        //更新result数据
        getResultData(text);
        if(mLvHistoryText.getAdapter()==null){
            mLvHistoryText.setAdapter(mHistoryAdapter);
        }else{
            mHistoryAdapter.notifyDataSetChanged();
        }
        Intent intent= new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(UrlContants.YADEX_RU_API_SEARCH+text);
        intent.setData(content_url);
        startActivity(intent);
        Toast.makeText(this, "完成搜素", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onShowYanderMessage(String text) {
        HttpUtil.getHot(CustomActivity.this, text, new HttpUtil.OnGetHotListener() {
            @Override
            public void onSuccess(List<String> words) {
                /*for(int i= 0;i<words.size();i++)
                {
                    if(!mHistoryKeywords.contains(words.get(i)))
                    {
                        mHistoryKeywords.add(words.get(i));
                        autoCompleteAdapter.notifyDataSetChanged();
                    }
                }*/
                autoCompleteData.addAll(words);
                autoCompleteAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onShowHistory() {
        mLlHistory.setVisibility(View.VISIBLE);
    }

    /**
     * 退出界面
     */
    @Override
    public void onFinish() {
             CustomActivity.this.finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.search_btn_deletehistory:
                clearHistory();
                break;
        }
    }


    @Override
    public void onSave(String text) {
        String oldText = mPref.getString(KEY_SEARCH_HISTORY_KEYWORD,"");
        if(!TextUtils.isEmpty(text) && !oldText.contains(text)){
            if(mHistoryKeywords.size()>9){
                Toast.makeText(this,"最多保存10条历史",Toast.LENGTH_SHORT).show();
                return;
            }
            Log.i("onSave",text);
            editor.putString(KEY_SEARCH_HISTORY_KEYWORD,text+","+oldText);
            editor.commit();
            mHistoryKeywords.add(0,text);
        }
        Log.e("存储",""+mHistoryKeywords.size());
        mHistoryAdapter.notifyDataSetChanged();
    }

    private void clearHistory(){
        editor.clear();
        mHistoryKeywords.clear();
        mHistoryAdapter.notifyDataSetChanged();
        mLlHistory.setVisibility(View.GONE);
        Toast.makeText(CustomActivity.this,"清除搜索历史成功",Toast.LENGTH_SHORT);
    }
}

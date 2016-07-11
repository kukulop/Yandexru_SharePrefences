package cn.searchdemo.yandexru.demo.view;


import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.searchdemo.yandexru.R;
import cn.searchdemo.yandexru.demo.util.HttpUtil;


public class SearchView extends LinearLayout implements View.OnClickListener {

    private EditText etInput;
    private ImageView ivDelete;
    private Button btnBack;
    private ListView lvTips;


    private List<String> mYandexWord;
    /**
     * 上下文对象
     */
    private Context mContext;

    /**
     * 联想框adapter （推荐adapter）
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

    private final int YanderMessage = 1;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case YanderMessage:
                    if(mListener!=null)
                    {
                        mListener.onShowYanderMessage((String) msg.obj);
                    }
            }
        }
    };
    /**
     * 设置搜索回调接口
     *
     * @param listener 监听者
     */
    public void setSearchViewListener(SearchViewListener listener) {
        mListener = listener;
    }

    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.search, this);
        initViews();
        initListener();
    }

    private void initViews() {
        etInput = (EditText) findViewById(R.id.search_et_input);
        ivDelete = (ImageView) findViewById(R.id.search_iv_delete);
        btnBack = (Button) findViewById(R.id.search_btn_back);
        lvTips = (ListView) findViewById(R.id.search_lv_tips);
    }

    private void initListener(){

        lvTips.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //set edit text
                String text = lvTips.getAdapter().getItem(i).toString();
                etInput.setText(text);
                etInput.setSelection(text.length());
                //hint list view gone and result list view show
                lvTips.setVisibility(View.GONE);
                notifyStartSearching(text);
            }
        });
        ivDelete.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        etInput.addTextChangedListener(new EditChangedListener());
        etInput.setOnClickListener(this);

        /**
         * 当EditText 编辑完之后点击软键盘上的搜索键才会触发
         */
        etInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    lvTips.setVisibility(GONE);
                    notifyStartSearching(etInput.getText().toString());;
                }
                return true;
            }
        });
    }

    /**
     * 设置自动补全adapter
     */
    public void setAutoCompleteAdapter(ArrayAdapter<String> adapter) {
        this.mAutoCompleteAdapter = adapter;
    }
    /**
     * 设置联想适配器 HintAdapter
     */
    public void setTipsHintAdapter(ArrayAdapter<String> adapter) {
        this.mHintAdapter = adapter;
        if (lvTips.getAdapter() == null) {
            lvTips.setAdapter(mHintAdapter);
        }
    }


    /**
     *
     * @param text
     */
    public void setEtInput(String text){
        etInput.setText(text);
    }


    private class EditChangedListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        /**
         * 当输入框的文字发生变更的时候
         * @param charSequence
         * @param i
         * @param i2
         * @param i3
         */
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            if(etInput.getText().toString().length()==0&&charSequence.toString().length()==0)
            {
                btnBack.setText("返回");
            }
            if (!"".equals(charSequence.toString())) {
                ivDelete.setVisibility(VISIBLE);
                lvTips.setVisibility(VISIBLE);
                btnBack.setText("Yanderux");
                /*
                发生网络搜索请求Get
                 */
                Message mess = new Message();
                mess.what = YanderMessage;
                mess.obj = etInput.getText().toString();
                handler.sendMessage(mess);

                //变更的文字不为空时
                if (mAutoCompleteAdapter != null && lvTips.getAdapter() != mAutoCompleteAdapter) {
                    lvTips.setAdapter(mAutoCompleteAdapter);
                }

                //更新autoComplete数据
                if (mListener != null) {
                    mListener.onRefreshAutoComplete(charSequence + "");
                }
            } else {
                ivDelete.setVisibility(GONE);
                if (mHintAdapter != null) {
                    lvTips.setAdapter(mHintAdapter);
                }
                lvTips.setVisibility(GONE);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_et_input:
                lvTips.setVisibility(VISIBLE);
                break;
            case R.id.search_iv_delete:
                if(mListener!=null)
                {
                    mListener.onShowHistory();
                }
                etInput.setText("");
                ivDelete.setVisibility(GONE);
                break;
            case R.id.search_btn_back:
                if(etInput.getText().toString().length()>0)
                {
                    //更新autoComplete数据
                    if (mListener != null) {
                        //保存数据至Share
                        mListener.onSave(etInput.getText().toString());
                        //开始搜索
                        mListener.onSearch(etInput.getText().toString());
                    }
                }else{
                    if (mListener != null) {
                        mListener.onFinish();
                    }
                }
                break;
        }
    }




    /**
     * 通知监听者 进行搜索操作
     * @param text
     */
    private void notifyStartSearching(String text){
        if (mListener != null) {
            mListener.onSearch(etInput.getText().toString());
        }

        //隐藏软键盘
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }


    /**
     * 当初始化时历史记录为空时，隐藏联想词显示框
     */
    public void hideLvTips(){
        lvTips.setVisibility(View.GONE);
    }
    /**
     * 当初始化时历史记录不为空时，显示联想词显示框
     */
    public void showLvTips(){
        lvTips.setVisibility(View.VISIBLE);
    }



    /**
     * search view回调方法
     */
    public interface SearchViewListener {
        void onShowYanderMessage(String text);

        /**
         * 显示搜索记录
         */
        void onShowHistory();
        /**
         * 退出界面
         */
        void onFinish();
        /**
         * 保存输入框中的内容至系统存储
         * @param text
         */
        void onSave(String text);
        /**
         * 更新自动补全内容
         *
         * @param text 传入补全后的文本
         */
        void onRefreshAutoComplete(String text);

        /**
         * 开始搜索
         *
         * @param text 传入输入框的文本
         */
        void onSearch(String text);

//        /**
//         * 提示列表项点击时回调方法 (提示/自动补全)
//         */
//        void onTipsItemClick(String text);
    }

}
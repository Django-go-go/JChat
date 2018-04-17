package com.jkingone.jchat.ui.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jkingone.jchat.Constant;
import com.jkingone.jchat.R;
import com.jkingone.jchat.adapter.ChatRecycleAdapter;
import com.jkingone.jchat.customview.SoftInputRelativeLayout;
import com.jkingone.jchat.imagePicker.ImagePickerActivity;
import com.jkingone.jchat.imagePicker.PreviewActivity;
import com.jkingone.jchat.utils.KeyBoardUtils;
import com.jkingone.jchat.utils.ScreenUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMConversationType;
import cn.bmob.newim.bean.BmobIMImageMessage;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMTextMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.core.ConnectionStatus;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.listener.MessageListHandler;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.v3.exception.BmobException;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    @BindView(R.id.toolbar_chat)
    Toolbar mToolbar;

    @BindView(R.id.img_chat_back)
    ImageView mImageView_back;

    @BindView(R.id.tv_chat_friend)
    TextView mTextView_name;

    @BindView(R.id.img_chat_info)
    ImageView mImageView_info;

    @BindView(R.id.img_chat_audio)
    ImageView mImageView_audio;

    @BindView(R.id.img_chat_keyboard)
    ImageView mImageView_keyboard;

    @BindView(R.id.img_chat_add)
    ImageView mImageView_add;

    @BindView(R.id.btn_chat_send)
    Button mButton_send;

    @BindView(R.id.btn_voice)
    Button mButton_voice;

    @BindView(R.id.edit_chat_send)
    EditText mEditText_send;

    @BindView(R.id.recycle_chat)
    RecyclerView mRecyclerView;

    @BindView(R.id.recycle_chat_bottom)
    RecyclerView mRecyclerView_bottom;

    private BmobIMConversation mConversation;
    private List<BmobIMMessage> mMessages = new ArrayList<>();
    private MessageListHandler mMessageListHandler;

    private ChatRecycleAdapter mChatRecycleAdapter;

    private String id;
    private String title;

    private MyHandler mMyHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ScreenUtils.setTranslucent(this);

        ButterKnife.bind(this);

        initToolbar();

        final Intent intent = getIntent();
        id = intent.getStringExtra("conversationId");
        title = intent.getStringExtra("conversationTitle");
        mTextView_name.setText(title);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mChatRecycleAdapter = new ChatRecycleAdapter(mMessages, this);
        mRecyclerView.setAdapter(mChatRecycleAdapter);
        mChatRecycleAdapter.setImageListener(new ChatRecycleAdapter.OnClickImageListener() {
            @Override
            public void onclickImage(String path) {
                Intent prev = new Intent(ChatActivity.this, PreviewActivity.class);
                prev.putExtra("preview", path);
                startActivity(prev);
            }
        });

        mRecyclerView_bottom.setLayoutManager(new GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false));
        mRecyclerView_bottom.setAdapter(new BottomAdapter());
        mRecyclerView_bottom.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                ScreenUtils.getScreenWidth(this) / 2));
        mRecyclerView_bottom.setVisibility(View.GONE);
        mRecyclerView_bottom.requestLayout();

        mEditText_send.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    mButton_send.setVisibility(View.GONE);
                    mImageView_add.setVisibility(View.VISIBLE);
                } else {
                    mButton_send.setVisibility(View.VISIBLE);
                    mImageView_add.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mEditText_send.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mRecyclerView_bottom.setVisibility(View.GONE);
                }
            }
        });

        mMyHandler = new MyHandler(new WeakReference<>(this));
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        int height = ScreenUtils.getStatusHeight(this) + ScreenUtils.getActionBarSize(this);
        SoftInputRelativeLayout.LayoutParams lp = new SoftInputRelativeLayout.LayoutParams(SoftInputRelativeLayout.LayoutParams.MATCH_PARENT, height);
        mToolbar.setLayoutParams(lp);
        mToolbar.requestLayout();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                mMyHandler.sendMessage(Message.obtain(mMyHandler, 1, queryConversation(id)));
            }
        });
        mMessageListHandler = new MessageListHandler() {
            @Override
            public void onMessageReceive(List<MessageEvent> list) {
                for (MessageEvent event : list) {
                    updateData(event);
                }
            }
        };
        BmobIM.getInstance().addMessageListHandler(mMessageListHandler);
    }

    @Override
    protected void onStop() {
        super.onStop();
        BmobIM.getInstance().removeMessageListHandler(mMessageListHandler);
    }

    private BmobIMConversation queryConversation(String id) {
        if (BmobIM.getInstance().getCurrentStatus().getCode() != ConnectionStatus.CONNECTED.getCode()) {
            return null;
        } else {
            List<BmobIMConversation> list = BmobIM.getInstance().loadAllConversation();
            for (BmobIMConversation conversation : list) {
                if (conversation.getConversationId().equals(id)) {
                    return conversation;
                }
            }
            return null;
        }
    }

    private void updateData(MessageEvent event) {
        BmobIMConversation conversation = event.getConversation();
        if (conversation.getConversationId().equals(id)) {
            List<BmobIMMessage> list = conversation.getMessages();
            if (list != null && list.size() > 0) {
                mMessages.add(0, list.get(0));
                mChatRecycleAdapter.notifyDataSetChanged();
                mRecyclerView.scrollToPosition(mMessages.size() - 1);
            }
            Log.i(TAG, "updateData: " + list);
        } else {
            Log.i(TAG, "updateData: not");
        }
    }

    private void sendText() {
        BmobIMUserInfo info = new BmobIMUserInfo(id, title, null);
        BmobIMConversation conversation = BmobIM.getInstance().startPrivateConversation(info, null);
        conversation.setConversationType(BmobIMConversationType.PRIVATE.getValue());
        BmobIMConversation messageManager = BmobIMConversation.obtain(BmobIMClient.getInstance(), conversation);
        final BmobIMTextMessage textMessage = new BmobIMTextMessage();
        textMessage.setContent(mEditText_send.getText().toString());
        messageManager.sendMessage(textMessage, new MessageSendListener() {
            @Override
            public void done(BmobIMMessage bmobIMMessage, BmobException e) {
                if (e != null) {
                    Log.e(TAG, "sendMessage: ", e);
                    Toast.makeText(ChatActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
                } else {
                    mEditText_send.getText().clear();
                    mMessages.add(0, bmobIMMessage);
                    mChatRecycleAdapter.notifyDataSetChanged();
                    mRecyclerView.scrollToPosition(mMessages.size() - 1);
                }
            }
        });
    }

    private void sendImage(List<String> paths) {

        BmobIMUserInfo info = new BmobIMUserInfo(id, title, null);
        BmobIMConversation conversation = BmobIM.getInstance().startPrivateConversation(info, null);
        conversation.setConversationType(BmobIMConversationType.PRIVATE.getValue());
        BmobIMConversation messageManager = BmobIMConversation.obtain(BmobIMClient.getInstance(), conversation);
        for (String path : paths) {
            final BmobIMImageMessage imageMessage = new BmobIMImageMessage(path);
            messageManager.sendMessage(imageMessage, new MessageSendListener() {
                @Override
                public void done(BmobIMMessage bmobIMMessage, BmobException e) {
                    if (e != null) {
                        Log.e(TAG, "sendMessage: ", e);
                        Toast.makeText(ChatActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
                    } else {
                        String path = bmobIMMessage.getContent();
                        path = path.substring(path.indexOf('&') + 1);
                        bmobIMMessage.setContent(path);
                        mMessages.add(0, bmobIMMessage);
                        mChatRecycleAdapter.notifyDataSetChanged();
                        mRecyclerView.scrollToPosition(mMessages.size() - 1);
                        Log.i(TAG, "done: " + bmobIMMessage);
                    }
                }
            });
        }

    }

    @OnClick({R.id.img_chat_info,
            R.id.img_chat_back,
            R.id.img_chat_audio,
            R.id.img_chat_add,
            R.id.btn_chat_send,
            R.id.edit_chat_send,
            R.id.img_chat_keyboard})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_chat_info: {
                Intent intent = new Intent(this, UserInfoActivity.class);
                intent.putExtra("conversationId", id);
                intent.putExtra("conversationTitle", title);
                startActivity(intent);
                break;
            }
            case R.id.img_chat_back: {
                finish();
                break;
            }
            case R.id.img_chat_keyboard: {
                mImageView_audio.setVisibility(View.VISIBLE);
                mImageView_keyboard.setVisibility(View.GONE);
                mRecyclerView_bottom.setVisibility(View.GONE);
                mButton_voice.setVisibility(View.GONE);
                mEditText_send.setVisibility(View.VISIBLE);
                KeyBoardUtils.openKeyboard(mEditText_send, this);
                break;
            }
            case R.id.img_chat_audio: {
                mImageView_keyboard.setVisibility(View.VISIBLE);
                mImageView_audio.setVisibility(View.GONE);
                mRecyclerView_bottom.setVisibility(View.GONE);
                mEditText_send.setVisibility(View.GONE);
                mButton_voice.setVisibility(View.VISIBLE);
                KeyBoardUtils.closeKeyboard(mEditText_send, this);
                break;
            }
            case R.id.img_chat_add: {
                if (mRecyclerView_bottom.getVisibility() == View.GONE) {
                    mRecyclerView_bottom.setVisibility(View.VISIBLE);
                    KeyBoardUtils.closeKeyboard(mEditText_send, this);
                } else {
                    mRecyclerView_bottom.setVisibility(View.GONE);
                }
                break;
            }
            case R.id.btn_chat_send: {
                sendText();
                break;
            }
            case R.id.edit_chat_send: {
                mRecyclerView_bottom.setVisibility(View.GONE);
            }
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.IMAGE_PICK && resultCode == RESULT_OK) {
            ArrayList<String> paths = data.getStringArrayListExtra("data");
            Log.i(TAG, "onActivityResult: " + paths);
            sendImage(paths);
        }
    }

    private static class MyHandler extends Handler {
        private WeakReference<ChatActivity> mReference;

        MyHandler(WeakReference<ChatActivity> weakReference) {
            mReference = weakReference;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ChatActivity chatActivity = mReference.get();
            chatActivity.mConversation = (BmobIMConversation) msg.obj;
            if (chatActivity.mConversation != null) {
                chatActivity.mMessages.addAll(chatActivity.mConversation.getMessages());
                chatActivity.mChatRecycleAdapter.notifyDataSetChanged();
                chatActivity.mRecyclerView.scrollToPosition(chatActivity.mMessages.size() - 1);
            }
        }
    }

    class BottomAdapter extends RecyclerView.Adapter<BottomAdapter.VH> {

        String[] titles = {"图片", "位置", "语音输入", "文件"};
        int[] imgs = {R.drawable.selector_img,
                R.drawable.selector_location,
                R.drawable.selector_phone,
                R.drawable.selector_files};

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(ChatActivity.this).inflate(R.layout.item_recycle_grid, parent, false));
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            holder.mImageView.setBackgroundResource(imgs[position]);
            holder.mTextView.setText(titles[position]);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ChatActivity.this, ImagePickerActivity.class);
                    intent.putExtra("MODE", ImagePickerActivity.IMAGE_MODE);
                    startActivityForResult(intent, Constant.IMAGE_PICK);
                }
            });
        }

        @Override
        public int getItemCount() {
            return titles.length;
        }

        class VH extends RecyclerView.ViewHolder {
            @BindView(R.id.img_recycle_img)
            ImageView mImageView;

            @BindView(R.id.tv_recycle_title)
            TextView mTextView;

            public VH(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ScreenUtils.getScreenWidth(ChatActivity.this) / 4,
                        ScreenUtils.getScreenWidth(ChatActivity.this) / 4);
                itemView.setLayoutParams(lp);
            }
        }
    }
}

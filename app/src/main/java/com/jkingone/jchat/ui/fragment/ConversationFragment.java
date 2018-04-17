package com.jkingone.jchat.ui.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jkingone.jchat.Constant;
import com.jkingone.jchat.R;
import com.jkingone.jchat.adapter.ConversationAdapter;
import com.jkingone.jchat.ui.activity.ChatActivity;
import com.jkingone.jchat.utils.ScreenUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.event.MessageEvent;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;

import static com.jkingone.jchat.utils.LogUtils.show;

/**
 * Created by Jkingone on 2018/4/7.
 */

public class ConversationFragment extends BaseFragment {

    private static final String TAG = "ConversationFragment";

    @BindView(R.id.recycle_conversation)
    RecyclerView mRecyclerView;

    @BindView(R.id.store_house_ptr_frame)
    PtrFrameLayout mPtrFrameLayout;

    private List<BmobIMConversation> mConversations = new ArrayList<>();
    private ConversationAdapter mConversationAdapter;

    private AlertDialog mAlertDialog;

    private List<String> mConversationIds = new ArrayList<>();

    private LoadConversationTask mConversationTask;

    private ListPopupWindow mListPopupWindow;

    @Override
    protected void onLazyLoadOnce() {
        mConversations.clear();
        mConversationTask.execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversation, container, false);
        ButterKnife.bind(this, view);

        initRecycleView();

        mConversationTask = new LoadConversationTask(new WeakReference<>(this));

        mPtrFrameLayout.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                mConversations.clear();
                List<BmobIMConversation> conversations = BmobIM.getInstance().loadAllConversation();
                if (conversations != null) {
                    mConversations.addAll(conversations);
                    mConversationAdapter.notifyDataSetChanged();
                }
                frame.refreshComplete();
            }
        });

        return view;
    }

    private void initRecycleView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mConversationAdapter = new ConversationAdapter(mConversations, getContext());
        mConversationAdapter.setListener(new ConversationAdapter.OnClickListener() {
            @Override
            public void onClick(BmobIMConversation conversation) {
                Intent intent = new Intent(ConversationFragment.this.getActivity(), ChatActivity.class);
                intent.putExtra("conversationId", conversation.getConversationId());
                intent.putExtra("conversationTitle", conversation.getConversationTitle());
                ConversationFragment.this.startActivityForResult(intent, Constant.CONVERSATION_CHAT);
            }
        });
        mConversationAdapter.setLongClickListener(new ConversationAdapter.OnLongClickListener() {
            @Override
            public void onLongClick(BmobIMConversation conversation, int pos) {
                createPopupFolderList(conversation, pos);
            }
        });
        mRecyclerView.setAdapter(mConversationAdapter);
    }

    private void initDialog(final BmobIMConversation conversation) {
        mAlertDialog = new AlertDialog.Builder(getContext())
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BmobIM.getInstance().deleteConversation(conversation);
                        mConversations.remove(conversation);
                        mConversationIds.add(conversation.getConversationId());
                        mConversationAdapter.notifyDataSetChanged();
                    }
                })
                .setTitle("确定要删除?")
                .create();
    }

    private void createPopupFolderList(final BmobIMConversation conversation, final int pos) {
        mListPopupWindow = new ListPopupWindow(getContext());
        mListPopupWindow.setAdapter(new PopWindowAdapter(conversation));
        mListPopupWindow.setWidth(ScreenUtils.getScreenWidth(getContext()) * 2 / 5);
        mListPopupWindow.setAnchorView(mRecyclerView.getChildAt(pos));
        mListPopupWindow.setModal(true);
        mListPopupWindow.setDropDownGravity(Gravity.END);

        mListPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (position == 2) {
                    if (conversation.getIsTop() != null && conversation.getIsTop()) {
                        conversation.setIsTop(false);
                    } else {
                        conversation.setIsTop(true);
                    }
                    mConversations.set(pos, conversation);
                    mConversationAdapter.notifyDataSetChanged();
                    BmobIM.getInstance().updateConversation(conversation);
                    mListPopupWindow.dismiss();
                } else if (position == 0) {
                    if (conversation.getUnreadCount() > 0) {
                        conversation.setUnreadCount(0);
                    } else {
                        conversation.setUnreadCount(1);
                    }
                    mConversations.set(pos, conversation);
                    mConversationAdapter.notifyDataSetChanged();
                    BmobIM.getInstance().updateConversation(conversation);
                    mListPopupWindow.dismiss();
                } else {
                    mListPopupWindow.dismiss();
                    if (mAlertDialog == null) {
                        initDialog(conversation);
                    }
                    mAlertDialog.show();
                }
            }
        });
        mListPopupWindow.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void updateEvent(MessageEvent event) {
        updateUI(event);
    }

    private void updateUI(MessageEvent event) {
        BmobIMConversation conversation = event.getConversation();
        Log.i(TAG, "BmobIMConversation: =====>\n" + show(conversation));
        if (mConversationIds.contains(conversation.getConversationId())) {
            return;
        }
        for (int i = 0; i < mConversations.size(); i++) {
            BmobIMConversation c = mConversations.get(i);
            if (c.getId().equals(conversation.getId())
                    || c.getConversationId().equals(conversation.getConversationId())) {
                mConversations.set(i, conversation);
                break;
            }

            if (i == mConversations.size() - 1) {
                mConversations.add(conversation);
            }
        }

        if (mConversations.size() == 0) {
            mConversations.add(conversation);
        }

        mConversationAdapter.notifyDataSetChanged();
    }

    class PopWindowAdapter extends BaseAdapter {

        String[] data = {"标为已读", "清除聊天记录", "聊天置顶"};

        BmobIMConversation mConversation;

        public PopWindowAdapter(BmobIMConversation conversation) {
            mConversation = conversation;
        }

        @Override
        public int getCount() {
            return data.length;
        }

        @Override
        public Object getItem(int position) {
            return data[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(ConversationFragment.this.getContext())
                        .inflate(R.layout.item_popwindow, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (position == 2) {
                if (mConversation.getIsTop() != null && mConversation.getIsTop()) {
                    holder.mTextView.setText("取消置顶");
                } else {
                    holder.mTextView.setText(data[position]);
                }
            } else if (position == 0) {
                if (mConversation.getUnreadCount() > 0) {
                    holder.mTextView.setText(data[position]);
                } else {
                    holder.mTextView.setText("标为未读");
                }
            } else {
                holder.mTextView.setText(data[position]);
            }
            return convertView;
        }

        class ViewHolder {
            @BindView(R.id.tv_popwindow)
            TextView mTextView;

            ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }

        }
    }

    private static class LoadConversationTask extends AsyncTask<Void, Integer, List<BmobIMConversation>> {
        WeakReference<ConversationFragment> mReference;

        LoadConversationTask(WeakReference<ConversationFragment> reference) {
            mReference = reference;
        }

        @Override
        protected List<BmobIMConversation> doInBackground(Void... voids) {
            return BmobIM.getInstance().loadAllConversation();
        }

        @Override
        protected void onPostExecute(List<BmobIMConversation> conversations) {
            if (conversations != null) {
                mReference.get().mConversations.addAll(conversations);
                mReference.get().mConversationAdapter.notifyDataSetChanged();
            }
        }
    }
}

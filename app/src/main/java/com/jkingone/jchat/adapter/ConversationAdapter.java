package com.jkingone.jchat.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jkingone.jchat.R;
import com.jkingone.jchat.utils.TimeUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMMessageType;

/**
 * Created by Jkingone on 2018/4/10.
 */

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

    private List<BmobIMConversation> mConversations;
    private Context mContext;
    private LayoutInflater mInflater;

    public ConversationAdapter(List<BmobIMConversation> conversations, Context context) {
        mConversations = conversations;
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.item_recycle_conversation, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final BmobIMConversation conversation = mConversations.get(position);
        if (conversation.getMessages() != null && conversation.getMessages().size() > 0) {
            BmobIMMessage message = conversation.getMessages().get(0);
            if (message.getMsgType().equals(BmobIMMessageType.IMAGE.getType())) {
                holder.mTextView_msg.setText("图片");
            } else if (message.getMsgType().equals(BmobIMMessageType.TEXT.getType())) {
                holder.mTextView_msg.setText(message.getContent());
            } else if (message.getMsgType().equals(BmobIMMessageType.LOCATION.getType())) {
                holder.mTextView_msg.setText("位置消息");
            } else if (message.getMsgType().equals(BmobIMMessageType.VIDEO.getType())) {
                holder.mTextView_msg.setText("视频");
            } else if (message.getMsgType().equals(BmobIMMessageType.VOICE.getType())) {
                holder.mTextView_msg.setText("语音消息");
            } else {
                holder.mTextView_msg.setText("文件");
            }
        }
        holder.mTextView_name.setTypeface(Typeface.DEFAULT_BOLD);
        holder.mTextView_name.setText(conversation.getConversationTitle());
        holder.mTextView_date.setText(TimeUtils.formatDateString(mContext, conversation.getUpdateTime()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mListener != null) {
                    mListener.onClick(conversation);
                }
            }
        });
        if (conversation.getIsTop() != null && conversation.getIsTop()) {
            holder.itemView.setBackgroundResource(R.color.pink_half);
        } else {
            holder.itemView.setBackgroundResource(R.color.white);
        }
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mLongClickListener != null) {
                    mLongClickListener.onLongClick(conversation, position);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mConversations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.img_avatar_conversation)
        ImageView mImageView;

        @BindView(R.id.tv_name_conversation)
        TextView mTextView_name;

        @BindView(R.id.tv_msg_conversation)
        TextView mTextView_msg;

        @BindView(R.id.tv_date)
        TextView mTextView_date;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private OnClickListener mListener;

    public void setListener(OnClickListener listener) {
        mListener = listener;
    }

    public interface OnClickListener {
        void onClick(BmobIMConversation conversation);
    }

    private OnLongClickListener mLongClickListener;

    public void setLongClickListener(OnLongClickListener longClickListener) {
        mLongClickListener = longClickListener;
    }

    public interface OnLongClickListener {
        void onLongClick(BmobIMConversation conversation, int pos);
    }
}

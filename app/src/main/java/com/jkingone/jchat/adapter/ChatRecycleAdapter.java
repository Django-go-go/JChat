package com.jkingone.jchat.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jkingone.jchat.Constant;
import com.jkingone.jchat.R;
import com.jkingone.jchat.bean.User;
import com.jkingone.jchat.utils.ScreenUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMMessageType;
import cn.bmob.newim.bean.BmobIMVideoMessage;
import cn.bmob.v3.BmobUser;

/**
 * Created by Jkingone on 2018/4/11.
 */

public class ChatRecycleAdapter extends RecyclerView.Adapter {
    private static final int DEFAULT = 1;
    private static final int REVERSE = 2;

    private List<BmobIMMessage> mMessages;
    private Context mContext;

    private LayoutInflater mInflater;

    public ChatRecycleAdapter(List<BmobIMMessage> messages, Context context) {
        mMessages = messages;
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case REVERSE:
                viewHolder = new ReverseViewHolder(mInflater.inflate(R.layout.item_recycle_chat_reverse, parent, false));
                break;
            case DEFAULT:
                viewHolder = new ViewHolder(mInflater.inflate(R.layout.item_recycle_chat, parent, false));
                break;
            default:
                viewHolder = null;
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int size = mMessages.size();
        final BmobIMMessage message = mMessages.get(size - position - 1);
        if (holder instanceof ViewHolder) {
            ViewHolder hold = (ViewHolder) holder;
            if (message.getMsgType().equals(BmobIMMessageType.IMAGE.getType())) {
                hold.mTextView.setVisibility(View.GONE);
                hold.mImageView_msg.setVisibility(View.VISIBLE);
                hold.mImageView_msg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mImageListener != null) {
                            mImageListener.onclickImage(message.getContent());
                        }
                    }
                });
                Picasso.with(mContext).load(message.getContent())
                        .centerCrop()
                        .resize(ScreenUtils.getScreenWidth(mContext) / 2,
                                ScreenUtils.getScreenWidth(mContext) / 2)
                        .into(hold.mImageView_msg);
            } else if (message.getMsgType().equals(BmobIMMessageType.TEXT.getType())) {
                hold.mTextView.setText(message.getContent());
                hold.mTextView.setVisibility(View.VISIBLE);
                hold.mImageView_msg.setVisibility(View.GONE);
            } else if (message.getMsgType().equals(BmobIMMessageType.LOCATION.getType())) {
            } else if (message.getMsgType().equals(BmobIMMessageType.VIDEO.getType())) {
            } else if (message.getMsgType().equals(BmobIMMessageType.VOICE.getType())) {
            } else {
                 hold.mTextView.setVisibility(View.GONE);
                 hold.mImageView_msg.setBackgroundResource(R.drawable.rc_file_icon_file);
                 hold.mImageView_msg.setVisibility(View.VISIBLE);
            }

            return;
        }
        if (holder instanceof ReverseViewHolder) {
            ReverseViewHolder hold = (ReverseViewHolder) holder;
            if (message.getMsgType().equals(BmobIMMessageType.IMAGE.getType())) {
                hold.mTextView.setVisibility(View.GONE);
                hold.mImageView_msg.setVisibility(View.VISIBLE);
                hold.mImageView_msg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mImageListener != null) {
                            mImageListener.onclickImage(message.getContent());
                        }
                    }
                });
                Picasso.with(mContext).load(message.getContent())
                        .centerCrop()
                        .resize(ScreenUtils.getScreenWidth(mContext) / 2,
                                ScreenUtils.getScreenWidth(mContext) / 2)
                        .into(hold.mImageView_msg);
            } else if (message.getMsgType().equals(BmobIMMessageType.TEXT.getType())) {
                hold.mTextView.setText(message.getContent());
                hold.mTextView.setVisibility(View.VISIBLE);
                hold.mImageView_msg.setVisibility(View.GONE);
            } else if (message.getMsgType().equals(BmobIMMessageType.LOCATION.getType())) {
            } else if (message.getMsgType().equals(BmobIMMessageType.VIDEO.getType())) {
            } else if (message.getMsgType().equals(BmobIMMessageType.VOICE.getType())) {
            } else {
                hold.mTextView.setVisibility(View.GONE);
                hold.mImageView_msg.setBackgroundResource(R.drawable.rc_file_icon_file);
                hold.mImageView_msg.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mMessages.size() <= 0) {
            return super.getItemViewType(position);
        } else {
            int size = mMessages.size();
            BmobIMMessage message = mMessages.get(size - position - 1);
            if (message.getFromId().equals(getMyUid())) {
                return REVERSE;
            } else {
                return DEFAULT;
            }
        }

    }

    private OnClickImageListener mImageListener;

    public void setImageListener(OnClickImageListener imageListener) {
        mImageListener = imageListener;
    }

    public interface OnClickImageListener {
        void onclickImage(String path);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img_chat_msg)
        ImageView mImageView_msg;

        @BindView(R.id.img_chat_avatar)
        ImageView mImageView_avatar;

        @BindView(R.id.tv_chat_msg)
        TextView mTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class ReverseViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.img_chat_msg_reverse)
        ImageView mImageView_msg;

        @BindView(R.id.img_chat_avatar_reverse)
        ImageView mImageView_avatar;

        @BindView(R.id.tv_chat_msg_reverse)
        TextView mTextView;

        public ReverseViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private static String getMyUid() {
        return BmobUser.getCurrentUser(User.class).getObjectId();
    }
}

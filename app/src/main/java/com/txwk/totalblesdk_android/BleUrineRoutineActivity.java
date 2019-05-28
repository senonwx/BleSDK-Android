package com.txwk.totalblesdk_android;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.txwk.totalblesdk_android.adapter.RecycleHolder;
import com.txwk.totalblesdk_android.adapter.RecyclerAdapter;
import com.txwk.totalblesdk_android.bean.UrineDetail;
import com.txwk.totalblesdk_android.bean.UrineIsBind;
import com.txwk.totalblesdk_android.util.BleUrineRoutinUtils;
import com.txwk.totalblesdk_android.util.LogUtils;
import com.txwk.totalblesdk_android.util.OkhttpUtils;
import com.txwk.totalblesdk_android.util.PreferenceTool;
import com.txwk.totalblesdk_android.util.ToastUtils;
import com.zhouwei.mzbanner.MZBannerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class BleUrineRoutineActivity extends BaseActivity {
    private static final String TAG = "BleUrineRoutineActivity";
    private RecyclerView recyclerView;
    private MZBannerView banner;
    private TextView connetstatus_tv,descripte_tv;
    private String userToken;//尿大夫userToken
    private int recordId;//尿大夫上传记录id
    private UrineDetail detail;//测量到的数据
    private final int REQUEST_CAMERA = 1000;
    private String timeTemp;
    private String userName;//上传到尿大夫后台的用户名
    private String cameraPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
            Environment.DIRECTORY_DCIM + File.separator + "Camera" + File.separator;

    @Override
    public int getLayoutId() {
        return R.layout.activity_ble_urine_routine;
    }

    @Override
    public void init() {
        initView();
        initBanner();
    }

    /**
     * 初始化View
     */
    private void initView() {
        recyclerView = findViewById(R.id.recyclerView);
        banner = findViewById(R.id.banner);
        connetstatus_tv = findViewById(R.id.connetstatus_tv);
        descripte_tv = findViewById(R.id.descripte_tv);
    }

    /**
     * 初始化Banner
     */
    private void initBanner() {
        // 父类设置数据
        initBanner(banner,
                Arrays.asList(new Integer[]{R.drawable.ble_urineroutine_1,R.drawable.ble_urineroutine_2,R.drawable.ble_urineroutine_3,
                        R.drawable.ble_urineroutine_4,R.drawable.ble_connet}),
                Arrays.asList(new String[]{"第一步:\n          取尿，然后把尿杯里的尿液导入试管，约占试管的4/5左右。(如果没有尿杯和试管可以用干净的纸杯替代)",
                        "第二步:\n          将试纸垂直放入试管中蘸取尿液，试纸上的色条要全部侵入尿液2秒。",
                        "第三步:\n          在吸水纸上，将残留在试纸两侧和背面的尿液吸干，以免交叉感染。",
                        "第四步:\n          把白色拍照底板拿出来放在平整的桌子上，将侵泡过尿液的试纸，按照上图的示例摆放在底板上。(注意试纸的摆放方向)",
                        "第五步:\n          点击下面圆形按钮，进入拍照界面。拍照完成点击确认上传图片分析结果。"}));
    }

    public void 点击拍照(View view) {
        banner.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        getBind();
    }

    /**
     * 获取usertoken
     */
    public void getBind() {
        addSweetDialog("正在操作");
        long time = System.currentTimeMillis() / 1000;
        FormBody formBody = new FormBody
                .Builder()
                // 任何字符串 但若调取的模式为无感知绑定模式，所传参数userbind须为手机号
                .add("userbind", (userName == null || userName.isEmpty()) ? "user": userName)
                .add("appkey",BleUrineRoutinUtils.appKey)
                .add("sign", BleUrineRoutinUtils.getSign(time))
                .add("atime", time + "")
                .build();
        final Request request = new Request.Builder()
                .url(BleUrineRoutinUtils.BASE_URL+"v2/usersign/isbind")
                .post(formBody)
                .build();
        Call call = OkhttpUtils.getHttpClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtils.e(e.getMessage());
                dissmissDialog();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                dissmissDialog();
                final String responseStr = response.body().string();
                Gson gson = new Gson();
                final UrineIsBind urine = gson.fromJson(responseStr, UrineIsBind.class);
                if (urine.getError() == 0) {
                    userToken = urine.getData().getUsertoken();
                    handler.sendEmptyMessage(0);
                } else {
                    Message message = Message.obtain();
                    message.what = 1;
                    message.obj = urine.getError_msg();
                    handler.sendMessage(message);
                }
            }
        });
    }

    private void takePhoto() {
        String state = Environment.getExternalStorageState();
        /*判断是否有SD卡*/
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            File file = createOriImageFile();
            Uri imgUriOri;
            if (file != null) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    imgUriOri = Uri.fromFile(file);
                } else {
                    //改变Uri  com.txwk.familydoctor.fileProvider注意和xml中的一致
                    imgUriOri = FileProvider.getUriForFile(this, "com.txwk.totalblesdk_android.fileprovider", file);
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUriOri);
                startActivityForResult(intent, REQUEST_CAMERA);
            }
        } else {
            ToastUtils.initToast("内存卡不存在");
        }
    }

    /**
     * 创建拍照需要的文件
     */
    public File createOriImageFile() {
        timeTemp = System.currentTimeMillis() + "";
        File file = new File(cameraPath+timeTemp + ".png");
        if (!file.exists()) {
            file.getParentFile().mkdirs();  //注意此处创建文件调用getParentFile()再调用    mkdirs()
        }
        return file;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA) {//指定uri 将不会回传data=null
            File file = new File(cameraPath+timeTemp + ".png");
            uploadImg(file);
        }
    }


    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.what == 0){
                takePhoto();
//                File file = new File(cameraPath+ "1559033610626.png");
//                uploadImg(file);
            }else if(msg.what == 1){
                ToastUtils.initToast((String) msg.obj);
            }else if(msg.what == 2){
                getDetails(recordId);
            }else if(msg.what == 3){
                banner.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                initRecyclerView();
            }
            return false;
        }
    });
    private void uploadImg(File file) {
        addSweetDialog("正在上传图片");
        long time = System.currentTimeMillis() / 1000;
        //表单
        RequestBody photoRequestBody = RequestBody.create(MediaType.parse("image/png"), file);
        MultipartBody.Part photo = MultipartBody.Part.createFormData("file", file.getName(), photoRequestBody);
        //参数
//        RequestBody usertokenb = RequestBody.create(MediaType.parse("text/plain"), userToken);
//        RequestBody atimeb = RequestBody.create(MediaType.parse("text/plain"), time+"");
//        RequestBody checkb = RequestBody.create(MediaType.parse("text/plain"), CodeUtils.getCheck(time,userToken));
//        RequestBody project_idb = RequestBody.create(MediaType.parse("text/plain"), "1");
        //project_id   1:尿常规11项  2：尿常规14项
//        ServerUtils.getNiaoDaiFuApi().uploadImg(photo,usertokenb,atimeb,checkb,project_idb)

        MultipartBody.Builder mb = new MultipartBody.Builder().setType(MultipartBody.FORM);
        mb.addFormDataPart("usertoken",userToken);
        mb.addFormDataPart("atime",System.currentTimeMillis() / 1000 + "");
        mb.addFormDataPart("check",BleUrineRoutinUtils.getCheck(time, userToken));
        mb.addFormDataPart("project_id","2");
        mb.addPart(photo);

        final Request request = new Request.Builder()
                .url(BleUrineRoutinUtils.BASE_URL+"v2/njuserdata/uploadimg")
                .post(mb.build())
                .build();
        Call call = OkhttpUtils.getHttpClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtils.e(e.getMessage());
                dissmissDialog();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                dissmissDialog();
                final String responseStr = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(responseStr);
                    if (jsonObject.getInt("error") == 0) {
                        recordId = jsonObject.getJSONObject("data").getInt("record_id");
                        handler.sendEmptyMessage(2);
                    } else {
                        Message message = Message.obtain();
                        message.what = 1;
                        message.obj = jsonObject.getString("error_msg");
                        handler.sendMessage(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void getDetails(int record_id) {
        addSweetDialog("正在分析");
        long time = System.currentTimeMillis() / 1000;

        FormBody formBody = new FormBody
                .Builder()
                .add("appkey",BleUrineRoutinUtils.appKey)
                .add("sign", BleUrineRoutinUtils.getSign(time))
                .add("atime", time + "")
                .add("record_id", record_id + "")
                .build();
        final Request request = new Request.Builder()
                .url(BleUrineRoutinUtils.BASE_URL+"v2/result/details")
                .post(formBody)
                .build();

        Call call = OkhttpUtils.getHttpClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtils.e(e.getMessage());
                dissmissDialog();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                dissmissDialog();
                final String responseStr = response.body().string();
                Gson gson = new Gson();
                detail = gson.fromJson(responseStr, UrineDetail.class);
                if (detail.getError() == 0) {
                    handler.sendEmptyMessage(3);
                } else {
                    Message message = Message.obtain();
                    message.what = 1;
                    message.obj = detail.getError_msg();
                    handler.sendMessage(message);
                }
            }
        });

    }

    //展示数据
    private void initRecyclerView(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerAdapter<UrineDetail.DataBean.DetailDataBean> adapter = new RecyclerAdapter<UrineDetail.DataBean.DetailDataBean>
                (this, detail.getData().getDetail_data(), R.layout.item_urineroutine_list) {
            @Override
            public void convert(final RecycleHolder helper, final UrineDetail.DataBean.DetailDataBean data, final int position) {
                helper.setVisible(R.id.lay,position == 0 ? true : false);
                if(position == detail.getData().getDetail_data().size() -1){
                    helper.setVisible(R.id.lay1,true);
                    helper.setText(R.id.reason_tv,detail.getData().getClinical_desc());
                    helper.setText(R.id.advice_tv,detail.getData().getExpert_advice());
                }else{
                    helper.setVisible(R.id.lay1,false);
                }

                helper.setText(R.id.name,data.getCname());
                helper.setText(R.id.unit,data.getUnit());
                helper.setText(R.id.result,data.getRecord_detail_value());
                helper.setText(R.id.around,data.getStandard_value());
                helper.setTextColor(R.id.result,data.getDetail_status().equals("0") ? R.color.login_: R.color.urine_text_red);
            }
        };
        recyclerView.setAdapter(adapter);
    }


}

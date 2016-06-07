package zumzum.app.rewi.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class LogSaver {
        static final SimpleDateFormat LOG_FILE_FORMAT = new SimpleDateFormat(
                        "yyyy-MM-dd-HH-mm-ssZ");
        private static final Executor EX = Executors.newSingleThreadExecutor();
       
        private Context mContext;


        public LogSaver(Context context) {
                mContext = context;

        }

        public File save() {
                final File path = new File(Environment.getExternalStorageDirectory(),
                                "alogcat");
                final File file = new File(path + "/alogcat."
                                + LOG_FILE_FORMAT.format(new Date()) + ".txt");

                String msg = "saving log to: " + file.toString();
                //Log.d("alogcat", msg);

                EX.execute(new Runnable() {
                        public void run() {
                        	
                        	Process process = null;
							try {
								process = Runtime.getRuntime().exec("logcat -e");
							} catch (IOException e2) {
								// TODO Auto-generated catch block
								e2.printStackTrace();
							}
                        	

                       	 BufferedReader bufferedReader = new BufferedReader(
                       	     new InputStreamReader(process.getInputStream()));

                       	 StringBuilder log=new StringBuilder();
                       	 String line = "";
                       	 try {
							while ((line = bufferedReader.readLine()) != null) {
							     log.append(line);
							 }
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
                                String dump = log.toString();

                                if (!path.exists()) {
                                        path.mkdir();
                                }

                                BufferedWriter bw = null;
                                try {
                                        file.createNewFile();
                                        bw = new BufferedWriter(new FileWriter(file), 1024);
                                        bw.write(dump);
                                } catch (IOException e) {
                                        Log.e("alogcat", "error saving log", e);
                                } finally {
                                        if (bw != null) {
                                                try {
                                                        bw.close();
                                                } catch (IOException e) {
                                                        Log.e("alogcat", "error closing log", e);
                                                }
                                        }
                                }
                        }
                });

                return file;
        }

}

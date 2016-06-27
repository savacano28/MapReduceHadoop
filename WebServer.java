
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class WebServer {
  public static class TokenizerMapper
       extends Mapper<Object, Text, Text, IntWritable>{

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

 public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      Text s=new Text("edams.ksc.nasa.gov");

     StringTokenizer itr = new StringTokenizer(value.toString(),"\t");

      if(itr.equals(s)){
        for (int i=0;i<4;i++){
        word.set(itr.nextToken());
        }
	context.write(word,one);
        }else{
	context.write(new Text("no"),new IntWritable(0));
        }

    }
 }

  public static class IntSumReducer
       extends Reducer<Text,IntWritable,Text,IntWritable> {
    private IntWritable result = new IntWritable();
	int max=0;
	Text mk=new Text();

    public void reduce(Text key, Iterable<IntWritable> values,
                       Context context
                       ) throws IOException, InterruptedException {
  
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }

      if(sum > max){
                max = sum;
                mk.set(key);
            }
             
            
      }
  
      protected void cleanup(Context context) throws IOException, InterruptedException {
      context.write(mk, new IntWritable(max));
  }}

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "server count");
    job.setJarByClass(WebServer.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);   
    
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}

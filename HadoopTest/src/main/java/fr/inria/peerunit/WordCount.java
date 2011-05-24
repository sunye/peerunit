package fr.inria.peerunit;

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
import org.apache.hadoop.util.GenericOptionsParser;


public class WordCount {
	
  private static Configuration config;
	
  public static class TokenizerMapper 
       extends Mapper<Object, Text, Text, IntWritable>{
    
    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();
      
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      StringTokenizer itr = new StringTokenizer(value.toString());
      while (itr.hasMoreTokens()) {
        word.set(itr.nextToken());
        context.write(word, one);
      }
    }
  }
  
  public static class IntSumReducer 
       extends Reducer<Text,IntWritable,Text,IntWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values, 
                       Context context
                       ) throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }

  public static Configuration setCfg(String address, String port) {

		Configuration cfg = new Configuration();

		String hostport = address+":"+port;

		cfg.set("mapred.job.tracker",hostport);

		return cfg;

  }

  public static void main(String[] arg) throws Exception {

      run(arg);

  }
  
  public static void run(String[] args) throws Exception {

    Configuration conf = setCfg(args[2],args[3]);

    //String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    if (args.length <4) {
      System.err.println("Usage: wordcount <in> <out> <jtAddr> <jtPort>");
      System.exit(2);
    }
    
    Job job = new Job(conf, "word count");
    job.setJarByClass(WordCount.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0])); //m2
    FileOutputFormat.setOutputPath(job, new Path(args[1])); //m3
    System.exit(job.waitForCompletion(true) ? 0 : 1); //m4
  }
}

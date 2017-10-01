import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

public class InvertedIndex {

	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		if (args.length != 2) {
			System.err.println("Usage: Word Count <input path> <output path>");
			System.exit(-1);
		}
		
		Job job = new Job();
		job.setJarByClass(InvertedIndex.class);
		job.setJobName("Word Count");
		
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		job.setMapperClass(WordCountMapper.class);
		job.setReducerClass(WordCountReducer.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.waitForCompletion(true);
	}
	
	public static class WordCountMapper extends Mapper<LongWritable, Text, Text, Text>{		
	    private Text word = new Text();
	    private Text DocID = new Text();
	    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {	    	
	    	String line = value.toString();
	    	StringTokenizer tokenizer = new StringTokenizer(line);
	    	DocID.set(tokenizer.nextToken());
	    	while (tokenizer.hasMoreTokens()) {
	    		word.set(tokenizer.nextToken());
	    		context.write(word, DocID);
	      }
	    }
	}
	
	public static class WordCountReducer extends Reducer<Text, Text, Text, Text> {		
		public void reduce(Text key, Iterable<Text> values, Context context ) throws IOException, InterruptedException {			
			HashMap<String, Integer> map = new HashMap<>();
			for (Text val : values) {
				String keyValue = val.toString();				
				Integer temp = map.get(keyValue);
				if (temp == null) {
					map.put(keyValue, 0);
				}				
				map.put(keyValue, map.get(keyValue) + 1);				
			}			
			StringBuilder sb = new StringBuilder();			
			for (Entry<String, Integer> entry : map.entrySet()) {
				sb.append("\t");
				sb.append(entry.getKey());
				sb.append(":");
				sb.append(entry.getValue());				
			}			
			Text result = new Text();
			result.set(sb.toString());			
			context.write(key, result);
		}
	}
}

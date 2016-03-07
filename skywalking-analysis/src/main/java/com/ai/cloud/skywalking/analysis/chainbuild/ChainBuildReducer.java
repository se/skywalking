package com.ai.cloud.skywalking.analysis.chainbuild;

import com.ai.cloud.skywalking.analysis.chainbuild.entity.CallChainTree;
import com.ai.cloud.skywalking.analysis.chainbuild.po.ChainInfo;
import com.ai.cloud.skywalking.analysis.config.ConfigInitializer;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;

public class ChainBuildReducer extends Reducer<Text, Text, Text, IntWritable> {
	private Logger logger = LoggerFactory.getLogger(ChainBuildReducer.class);

	@Override
	protected void setup(Context context) throws IOException,
			InterruptedException {
		ConfigInitializer.initialize();
	}

	@Override
	protected void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {
		doReduceAction(Bytes.toString(key.getBytes()), values.iterator());
	}

	public void doReduceAction(String key, Iterator<Text> chainInfoIterator)
			throws IOException, InterruptedException {
		CallChainTree chainTree = CallChainTree.load(key);

		while (chainInfoIterator.hasNext()) {
			String callChainData = chainInfoIterator.next().toString();
			ChainInfo chainInfo = null;
			try {
				chainInfo = new Gson().fromJson(callChainData, ChainInfo.class);
				if (chainInfo.getChainStatus() == ChainInfo.ChainStatus.NORMAL) {
					chainTree.processMerge(chainInfo);
				}
				// 合并数据
				chainTree.summary(chainInfo);
			} catch (Exception e) {
				logger.error(
						"Failed to summary call chain, maybe illegal data:"
								+ callChainData, e);
			}
		}

		chainTree.saveToHbase();
	}
}
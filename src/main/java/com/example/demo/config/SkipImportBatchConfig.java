package com.example.demo.config;

import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.domain.model.Employee;

@Configuration
public class SkipImportBatchConfig extends BaseConfig {

	/**
	 * Listener
	 */
	@Autowired
	private SkipListener<Employee, Employee> employeSkipListener;

	@Autowired
	private MyBatisBatchItemWriter<Employee> mybatisWriter;

	/**
	 * Stepの生成(Skip)
	 */
	@Bean
	public Step csvImportSkipStep() {
		return this.stepBuilderFactory.get("CsvImportSkipStep") // builderの取得
				.<Employee, Employee>chunk(10) // chunkの設定
				.reader(csvReader()).listener(this.readListener) // reader
				.processor(genderConvertProcessor).listener(this.processListener) // processor
				.writer(mybatisWriter) // writer
				.faultTolerant() // 親メソッドをオーバーライドして、新しい FaultTolerantStepBuilder が作成されないようにします
				.skipLimit(Integer.MAX_VALUE) // 最大件数
				.skip(RuntimeException.class) // 例外クラス
				.listener(this.employeSkipListener) // listener
				.build();
	}

	/**
	 * Jobの生成(Skip)
	 */
	@Bean("SkipJob")
	public Job csvImportSkipJob() {
		return this.jobBuilderFactory.get("CsvImportSkipJob") // builderの取得
				.incrementer(new RunIdIncrementer()) // IDのインクリメント
				.start(csvImportSkipStep()) // 最初のStep
				.build();
	}
}

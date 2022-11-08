package com.example.demo.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.mybatis.spring.batch.builder.MyBatisBatchItemWriterBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.domain.model.Employee;

@Configuration
public class MyBatisImportBatchConfig extends BaseConfig {

	/**
	 * SqlSessionFactory(MyBatisで必要)
	 */
	@Autowired
	private SqlSessionFactory sqlSessionFactory;

	/**
	 * Writer(MyBatis)
	 */
	@Bean
	public MyBatisBatchItemWriter<Employee> myBatisWriter() {
		return new MyBatisBatchItemWriterBuilder<Employee>() // Builder取得
				.sqlSessionFactory(sqlSessionFactory) // SqlSessionFactoryの指定（MyBatisでSQLを実行したりするクラス）
				.statementId("com.example.demo.repository.EmployeeMapper.insertOne") // SQLのIDを指定（mapperのnamespace属性+SQLのID属性を指定）
				.build(); //
	}

	/**
	 * Stepの生成(MyBatis)
	 */
	@Bean
	public Step csvImportMybatisStep() {
		return this.stepBuilderFactory.get("CsvImportMybatisStep") // Builderの取得
				.<Employee, Employee>chunk(10) // chunkの設定
				.reader(csvReader()).listener(this.readListener) // readerセット
				.processor(compositeProcessor()).listener(this.processListener) // processorセット
				.writer(myBatisWriter()).listener(this.writeListener) // writerセット
				.build(); // Stepの生成
	}

	/**
	 * Jobの生成(MyBatis)
	 */
	@Bean("MybatisJob")
	public Job csvImportMybatisJob() {
		return this.jobBuilderFactory.get("CsvImportMybatisJob") // Builderの取得
				.incrementer(new RunIdIncrementer()) // IDのインクリメント
				.start(csvImportMybatisStep()) // 最初のStep
				.build(); // Jobの生成
	}
}

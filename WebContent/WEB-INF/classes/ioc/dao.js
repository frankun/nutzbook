var ioc = {
		conf:{
			type:"org.nutz.ioc.impl.PropertiesProxy",
			fields:{
				paths:["custom/"]
			}
		},
        dataSource : {
            type : "com.alibaba.druid.pool.DruidDataSource",
            events : {
                create : "init",
                depose : 'close'
            },
            fields : {
                url : {java:"$conf.get('db.url')"},
                username : {java:"$conf.get('db.username')"},
                password : {java:"$conf.get('db.password')"},
                testWhileIdle : true, // 非常重要,预防mysql的8小时timeout问题
                validationQuery : {java:"$conf.get('db.validationQuery')"}, // Oracle的话需要改一下
                maxActive : {java:"$conf.get('db.maxActive')"},
                filters : "mergeStat",
                connectionProperties : "druid.stat.slowSqlMillis=2000"
            }
        },
        dao : {
            type : "org.nutz.dao.impl.NutDao",
            args : [{refer:"dataSource"}]
        }
};
# Neptune Console

## 简介

利用百度开源Huge Graph的前端模块实现Aws Neptune图数据库的可视化操作控制台

- neptune-console-api：neptune操作控制台API
- neptune-console-dao-starter：neptune数据访问层封装
- neptune-console-ui：neptune操作可视化前端

## 特性

* 通过顶点和边绘制图形
* 显示顶点、边和模式的数据详细信息
* 提供语法突出显示、智能代码完成的智能Gremlin编辑器

## 源码构建

* 源码下载：

```bash
git clone https://github.com/transsnet/neptune-console.git
# 或者
git clone git@github.com:transsnet/neptune-console.git
```

* 进入项目根目录 `neptune-console`
* 安装前端依赖：

```bash
cd neptune-console-ui
npm install
```

* 构建：回到项目根目录执行`build.sh`脚本

```
./build.sh
```

* 启动运行：

```bash
./start.sh
```

* 访问：[http://localhost:9999/index.html](http://localhost:9999/index.html)

![image-20200107155614651](doc/01.png)

## 使用Gremlin语言创建一个图

* 创建顶点

```java
g.addV('person').property(id,'1').property('name','tom');
g.addV('person').property(id,'2').property('name','jack');
g.addV('software').property(id,'3').property('lang','java');
```

![image-20200107155614651](doc/02.png)

* 创建边

```java
g.addE('uses').from(g.V('1')).to(g.V('3'));
g.addE('develops').from(g.V('2')).to(g.V('3'));
g.addE('knows').from(g.V('1')).to(g.V('2'));
```

![image-20200107155614651](doc/03.png)

* 查询

```java
g.E().hasLabel('uses', 'develops', 'knows');
```

![image-20200107155614651](doc/04.png)

![image-20200107155614651](doc/05.png)

![image-20200107155614651](doc/06.png)
一个idea翻译的小插件，可以翻译编辑器里的文本，并且记录单词到redis中，显示查询最多的10个单词。
需要有redis以及百度通用翻译的key。
配置文件在resource目录下。
  baidu.appid=百度通用文本翻译的appid
  baidu.sercuritykey=百度通用文本翻译的sercuritykey
  redis.host=redis的ip
  redis.port=哪个port
  redis.auth=redis密码
  redis.zset.name=创建的sorted set的名字（任意）
没有ui，自娱自乐。

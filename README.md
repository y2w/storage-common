使用一套API操作本地文件文件系统/阿里云OSS/go-fastdfs( https://github.com/sjqzhang/go-fastdfs ) 分布式文件系统，使用spring boot方式自动注入；
注入Storage即可

本地文件系统配置
1.nginx配置(nginx.conf)

    server {
        listen       8181;
        server_name  localhost;

        location ^~/oss {
          proxy_redirect     off;
          proxy_set_header X-Real-IP $remote_addr;
          proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        
          proxy_set_header Host 127.0.0.1;
          proxy_pass http://127.0.0.1/;
        }

	      location ~.*\.(html|shtml|htm|htc|xml|mp3|gif|jpg|png|bmp|swf|css|js)$ {
           proxy_cache  cache_one;
           proxy_cache_valid 200 304 12h;
           proxy_cache_valid 301 302 1m;
           proxy_cache_valid any 1m;
           proxy_cache_key $host$uri$is_args$args;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_pass http://localhost:8182;
           expires 1d;
        }

        location /nginx_status {
           stub_status on;
           allow all;
        }
    }

    server {
        listen       8182;
        server_name  localhost;

        location / {
            root   F:/test/data;
            index  index.html index.htm;
	   
	     if ($request_filename ~* ^.*?\.(txt|doc|pdf|rar|gz|zip|docx|exe|xlsx|ppt|pptx|jx)$){
		      add_header Content-Disposition: 'attachment;';
	     }

	        #auth_basic "please input username and passwd ";  
          #auth_basic_user_file passwd.db;
        }

        error_page  404   /404.jpg;

 
        error_page   500 502 503 504  /404.jpg;
        location = /50x.html {
            root   html;
        }
    }
    
    端口为8182
    
    
2.  yml配置指向nginx的ip以及端口
```yml
storage:
  active: filesystem
  fastdfs: 
     url: http://10.71.3.229
     group: group2
     support_group_manage: true 
  filesystem:
     path: F:\test\data
     url: http://localhost:8182
  aliyun:
     bucketName: 
     endpoint: 
     accessKeyId: 
     accessKeySecret: 
     requestTimeoutEnabled: true
     expiration: 600


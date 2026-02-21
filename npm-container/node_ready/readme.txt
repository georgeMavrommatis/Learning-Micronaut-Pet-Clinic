---> make sure the application is running on 8080 port
---> make sure you are located under "npm-container/node_ready/" and execute:
#1 docker-compose down -v
#2 docker-compose up -d

#3
    #for testing backpressure
        #A navigate to your browser at http://127.0.0.1:8000/websocket_stream_back_pressure.html
        #B hit load more with the number of your choice

    #for testing streaming json
        #A navigate to your browser at http://127.0.0.1:8000/json_stream_back_pressure.html


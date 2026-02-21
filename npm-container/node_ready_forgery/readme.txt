---> make sure the application is running on 8080 port
---> make sure you are located under "back-pressure-npm-container/node_ready/" and execute:
#1 docker-compose down -v
#2 docker-compose up -d


#3
    #for testing evil-csrf.html
        #navigate to your browser at http://127.0.0.1:8000/evil-csrf.html

    #for testing evil-csrf-form.html
        #navigate to your browser at http://127.0.0.1:8000/evil-csrf-form.html
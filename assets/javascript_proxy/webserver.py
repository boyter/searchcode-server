import string,cgi,time
from os import curdir, sep
from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer
import urllib2
import json

'''This is in here to be used as a reverse proxy back to the application however it will pass though
any javascript/css so that you don't need to recompile when working on them. When start runs on port 8888
and proxies back to 8080'''
class MyHandler(BaseHTTPRequestHandler):

  def do_GET(self):
    try:
      exclusions = 'script.js|style.css|newstyle.css|test_search.html|out.js'.split('|')

      if len([x for x in exclusions if x in self.path]) == 0:

        url = 'http://localhost:8080' + self.path
        data = urllib2.urlopen(url)
        data = data.read()

        self.send_response(200)
        if '.css' in self.path:
          self.send_header('Content-type', 'text/css')
        elif '.png' in self.path:
          self.send_header('Content-type', 'image/png')
        elif '.js' in self.path:
          self.send_header('Content-type', 'application/javascript')
        else:
          self.send_header('Content-type', 'text/html')
        self.end_headers()
        self.wfile.write(data)
        return

      # Split and get the last two parts IE css/js
      res = self.path.split('/')
      print 'Sourcing from disk %s' % (self.path)

      # Load the file directly to speed up testing
      f = open('../../src/main/resources/public/%s/%s' % (res[-2], res[-1]))
      self.send_response(200)
      if res[-2] == 'css':
        self.send_header('Content-type', 'text/css')
      elif res[-2] == 'js':
        self.send_header('Content-type', 'application/javascript')
      else:
        self.send_header('Content-type', 'text/html')

      self.end_headers()
      self.wfile.write(f.read())
      f.close()
      
          
      return
            
    except IOError:
        self.send_error(404,'File Not Found: %s' % self.path)
   

def main():
  try:
    server = HTTPServer(('', 8090), MyHandler)
    print 'started httpserver on 8090...'
    server.serve_forever()
  except KeyboardInterrupt:
    print '^C received, shutting down server'
    server.socket.close()

if __name__ == '__main__':
  main()


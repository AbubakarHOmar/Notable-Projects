from bs4 import BeautifulSoup as bs
import requests
from time import strptime
import datetime
import os
import sqlite3
from pytz import timezone
import re
cases=['posts','movies','batch']
headers={"User-Agent":"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.193 Safari/537.36"}
db=sqlite3.connect("test.db")
cursor=db.cursor()
def modification_date(filename):
    t = os.path.getmtime(filename)
    w=datetime.datetime.fromtimestamp(t)
    return w.astimezone(tz=timezone('US/Pacific')).date()
d = modification_date('test.db')
print(d)
def search(item):
    regex=re.compile('\d+')
    return regex.search(item).group()
    
def store_data():
    episode_number=post.find("h1",class_="name post-title entry-title titlehh").find("font",class_="aa_ss_ops",style=None).contents[0].strip()
    show_name=post.find("h1",class_="name post-title entry-title titlehh").find("font",class_="aa_ss_ops",style="margin: 0px 0px -4px 0px;").contents[0].strip()
    torrent_container=post.find_all("div", class_="col-12 col-sm-12 col-md-12 col-lg-12 col-xl-12 nonmain release-table button last_block")
    if 'V0' in episode_number:
        return
    if case == 'posts' and 'V2' in episode_number or 'v2' in episode_number or '(Multi)' in episode_number:
        epno=search(episode_number)
        cursor.execute("SELECT epno FROM Episodes WHERE showname = ? ORDER BY ROWID DESC LIMIT 1",(show_name,))
        last_inserted=cursor.fetchall()
        if len(last_inserted) > 0:
            check=search(last_inserted[0][0])
            if epno == check:
                cursor.execute("DELETE FROM Episodes WHERE showname = ? AND epno = ?",(show_name,last_inserted[0][0]))
                db.commit()
                for torrent in torrent_container:
                    quality=torrent.find("i", class_="sp_p_q").string
                    quality=quality[1:len(quality)-1]
                    torrent_link=torrent.a['href']
                    try:
                        cursor.execute("INSERT INTO Episodes VALUES (?,?,?,?)",(show_name,quality,episode_number,torrent_link))
                        db.commit()
                        print(show_name,episode_number,case)
                    except sqlite3.Error as error:
                        print(error)
            else:
                for torrent in torrent_container:
                    quality=torrent.find("i", class_="sp_p_q").string
                    quality=quality[1:len(quality)-1]
                    torrent_link=torrent.a['href']
                    try:
                        cursor.execute("INSERT INTO Episodes VALUES (?,?,?,?)",(show_name,quality,episode_number,torrent_link))
                        db.commit()
                        print(show_name,episode_number,case)
                    except sqlite3.Error as error:
                        print(error)
        else:
            for torrent in torrent_container:
                    quality=torrent.find("i", class_="sp_p_q").string
                    quality=quality[1:len(quality)-1]
                    torrent_link=torrent.a['href']
                    try:
                        cursor.execute("INSERT INTO Episodes VALUES (?,?,?,?)",(show_name,quality,episode_number,torrent_link))
                        db.commit()
                        print(show_name,episode_number,case)
                    except sqlite3.Error as error:
                        print(error)
            
                    
    else:
        for torrent in torrent_container:
            quality=torrent.find("i", class_="sp_p_q").string
            quality=quality[1:len(quality)-1]
            torrent_link=torrent.a['href']
            if case=='posts':
                try:
                    cursor.execute("INSERT INTO Episodes VALUES (?,?,?,?)",(show_name,quality,episode_number,torrent_link))
                    db.commit()
                    print(show_name,episode_number,case)
                except sqlite3.Error as error:
                    print(error)
            elif case=='movies':
                try:
                    cursor.execute("INSERT INTO Movies VALUES (?,?,?,?)",(show_name,quality,episode_number,torrent_link))
                    db.commit()
                    print(show_name,episode_number,case)
                except sqlite3.Error as error:
                    print(error)
            elif case=='batch':
                try:
                    cursor.execute("INSERT INTO Batch VALUES (?,?,?,?)",(show_name,quality,episode_number,torrent_link))
                    db.commit()
                    print(show_name,episode_number,case)
                except sqlite3.Error as error:
                    print(error)
def check_data(case):
    if case=='posts':
        cursor.execute("""SELECT * FROM Episodes
                          WHERE showname = ? AND epno = ?""",(show_name,episode_number))
        check=cursor.fetchall()
        if len(check)>0:
            pass
        else:
            store_data()
    elif case=='movies':
        cursor.execute("""SELECT * FROM Movies
                          WHERE showname = ? AND moviename = ?""",(show_name,episode_number))
        check=cursor.fetchall()
        if len(check)>0:
            pass
        else:
            store_data()
    elif case=='batch':
        cursor.execute("""SELECT * FROM Batch
                          WHERE showname = ? AND range = ?""",(show_name,episode_number))
        check=cursor.fetchall()
        if len(check)>0:
            pass
        else:
            store_data()
            
for case in cases:
    print(case)
    url=requests.get("https://www.erai-raws.info/" + case ,headers=headers).text
    soup=bs(url,"lxml")
    maxpage=soup.find("span",class_="page-numbers dots").find_next_sibling("a").string
    for i in range(1,int(maxpage)+1,1):
        url=requests.get("https://www.erai-raws.info/" + case + '/page/' + str(i) ,headers=headers).text
        soup=bs(url,"lxml")
        post=soup.find("article", class_="era_center col-12 col-sm-12 col-md-12 col-lg-12 col-xl-12 nonmain border_radius_22")
        if post.find("font",class_="clock_time_white"):
            date = datetime.datetime.strptime(post.find("font",class_="clock_time_white").string.strip()[2:], "%b %d, %Y").date()
        else:
            date=datetime.datetime.today()
            date=date.astimezone(tz=timezone('US/Pacific')).date()
        
        if date==d or date>d:
            continue
            
            
        elif date<d:
            s=i
            url=requests.get("https://www.erai-raws.info/" + case ,headers=headers).text
            soup=bs(url,"lxml")
            for j in range(s,0,-1):
                url=requests.get("https://www.erai-raws.info/" + case + '/page/' + str(j) ,headers=headers).text
                soup=bs(url,"lxml")
                posts=soup.find_all("article", class_="era_center col-12 col-sm-12 col-md-12 col-lg-12 col-xl-12 nonmain border_radius_22")
                posts=posts[::-1]
                for post in posts:
                    if post.find("font",class_="clock_time_white"):
                        pubdate=post.find("font",class_="clock_time_white").string.strip()[2:]
                        date = datetime.datetime.strptime(pubdate, "%b %d, %Y").date()
                        print(date, "white")
                        
                    
                    else:
                        date=datetime.datetime.today()
                        date=date.astimezone(tz=timezone('US/Pacific')).date()
                        print(date, "green")
                    
                        
                
                    if date==d:
                        episode_number=post.find("h1",class_="name post-title entry-title titlehh").find("font",class_="aa_ss_ops",style=None).contents[0].strip()
                        show_name=post.find("h1",class_="name post-title entry-title titlehh").find("font",class_="aa_ss_ops",style="margin: 0px 0px -4px 0px;").contents[0].strip()
                        if case=='posts':
                            check_data('posts')
                        elif case=='movies':
                            check_data('movies')
                        elif case=='batch':
                            check_data('batch')
                            
                            
                    elif date>d:
                        store_data()
        
        break



            
            
"""posts=soup.find_all("article", class_="era_center col-12 col-sm-12 col-md-12 col-lg-12 col-xl-12 nonmain border_radius_22")
    posts=posts[::-1]
    for post in posts:
        if post.find("font",class_="clock_time_white"):
            pubdate=post.find("font",class_="clock_time_white").string.strip()[2:]
            date = datetime.datetime.strptime(pubdate, "%b %d, %Y").date()
            print(date)
            
        
        else:
            date=datetime.datetime.today()
            date=date.astimezone(tz=timezone('US/Pacific')).date()
            print(date)
        
            
    
        if date==d:
            episode_number=post.find("h1",class_="name post-title entry-title titlehh").find("font",class_="aa_ss_ops",style=None).contents[0].strip()
            show_name=post.find("h1",class_="name post-title entry-title titlehh").find("font",class_="aa_ss_ops",style="margin: 0px 0px -4px 0px;").contents[0].strip()
            if case=='posts':
                check_data('posts')
            elif case=='movies':
                check_data('movies')
            elif case=='batch':
                check_data('batch')
                
        elif date>d:
            store_data()"""
            

        
    

package gds.music;

/**
 * Author:  gds
 * Time: 2016/5/20 22:42
 * E-mail: guodongshenggds@foxmail.com
 */
public class MusicInfo {

    private String title;
    private String album;
    private String singer;
    private String absPath;
    private long duration;
    private long size;

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getAbsPath() {
        return absPath;
    }

    public void setAbsPath(String absPath) {
        this.absPath = absPath;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    MusicInfo(String title, String album, String singer, String absPath, long duration, long size ){
        this.title = title;
        this.album = album;
        this.singer = singer;
        this.absPath = absPath;
        this.duration = duration;
        this.size = size;
    }


}

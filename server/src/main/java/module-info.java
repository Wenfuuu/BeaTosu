module beat.osu.server {
    requires java.sql;
    requires beat.osu.shared;
    requires static lombok;
    requires java.compiler;
    exports beat.osu.server;
}
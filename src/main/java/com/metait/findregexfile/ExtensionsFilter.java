package com.metait.findregexfile;


import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.file.AccumulatorPathVisitor;
import org.apache.commons.io.file.Counters;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
public class ExtensionsFilter implements FileFilter
{
    // private char[][] extensions;
    private String[] extensions;
    private List<String> listWildCards = null;
    private AccumulatorPathVisitor [] accumulatorPathVisitors = null;
    private Path dir = null;
    public void setPathDir(Path p_dir) { dir = p_dir; }
    public Path getPathDir() { return dir; }
    private List<String> getWildCards(List<String> listExtensionsOrWildcards)
    {
        List<String> ret = new ArrayList<String>();
        if (listExtensionsOrWildcards != null && listExtensionsOrWildcards.size()>0)
        {
            for(String str : listExtensionsOrWildcards)
            {
                if (str == null || str.trim().length()==0)
                    continue;
                if (str.contains("?") || str.contains("*"))
                    ret.add(str);
            }
        }
        if (ret.size() == 0)
            return null;
        return ret;
    }

    private String [] getExtensions(List<String> listExtensionsOrWildcards)
    {
        String [] ret = null;
        List<String> retList = new ArrayList<String>();
        if (listExtensionsOrWildcards != null && listExtensionsOrWildcards.size()>0)
        {
            for(String str : listExtensionsOrWildcards)
            {
                if (str == null || str.trim().length()==0)
                    continue;
                if (!str.contains("?") && !str.contains("*"))
                    retList.add(str);
            }
        }
        if (retList.size() == 0)
            return null;
        ret = new String[retList.size()];
        ret = retList.toArray(ret);
        return ret;
    }

    public ExtensionsFilter(List<String> listExtensionsOrWildcards)
    {
        // super(listExtensionsOrWildcards);
        listWildCards = getWildCards(listExtensionsOrWildcards);
        String [] arrExtensions = getExtensions(listExtensionsOrWildcards);
        initialize(arrExtensions);
        if (listWildCards != null && listWildCards.size()>0)
        {
            accumulatorPathVisitors = new AccumulatorPathVisitor[listWildCards.size()];
            int i = 0;
            for(String strWildCard : listWildCards)
            {
                accumulatorPathVisitors[i] = AccumulatorPathVisitor.withLongCounters(new WildcardFileFilter(strWildCard), DirectoryFileFilter.INSTANCE);
            }
        }
    }

    private void initialize(String[] extensions)
    {
        if (extensions == null || extensions.length == 0)
            return;
        int length = extensions.length;
        // this.extensions = new char[length][];
        this.extensions = new String[length];
        for (String s : extensions)
        {
            //this.extensions[--length] = s.toCharArray();
            this.extensions[--length] = s; // s.toLowerCase();
        }
    }
    public ExtensionsFilter(String[] extensions)
    {
        initialize(extensions);
    }

    @Override
    public boolean accept(File file)
    {
        String name = file.getName();
        if (name == null || name.trim().length() == 0)
            return false;
        if (extensions != null)
            for(String ext : extensions) {
                if (name.endsWith(ext))
                    return true;
                if (name.equals(ext))
                    return true;
            }

        if (listWildCards != null)
        {
            boolean ret = acceptPossibleWildCardFile(file);
            return ret;
        }
        return false;
            /*
            char[] path = file.getPath().toCharArray();
            for (char[] extension : extensions)
            {
                if (extension.length > path.length)
                {
                    continue;
                }
                int pStart = path.length - 1;
                int eStart = extension.length - 1;
                boolean success = true;
                for (int i = 0; i <= eStart; i++)
                {
                    if ((path[pStart - i] | 0x20) != (extension[eStart - i] | 0x20))
                    {
                        success = false;
                        break;
                    }
                }
                if (success)
                    return true;
            }
            return false;
             */
    }

    private boolean acceptPossibleWildCardFile(File file)
    {
        boolean ret = false;
        if (file != null)
        {
            if (dir == null || !dir.toFile().equals(file.getParentFile()))
                dir = Paths.get(file.getParentFile().getAbsolutePath());
            // final Path dir = Paths.get(file.getParentFile().getAbsolutePath());
            for (AccumulatorPathVisitor wildCard : accumulatorPathVisitors)
            {
                if (isWildCardFile(wildCard, dir, file))
                    return true;
            }
        }
        return ret;
    }

    public static boolean isWildCardFile(AccumulatorPathVisitor wildCard, Path dir, File file)
    {
        boolean ret = false;
        if (wildCard != null && file != null)
        {
            // final Path dir = Paths.get(file.getParentFile().getAbsolutePath());
            final AccumulatorPathVisitor visitor = wildCard;
            // Walk one dir
            try {
                Files.walkFileTree(dir, Collections.emptySet(), 1, visitor);
                Counters.PathCounters counters = visitor.getPathCounters();
                /*
                System.out.println(counters.getFileCounter().get());
                System.out.println(visitor.getFileList());
                 */
                if (counters.getFileCounter().get() > 0)
                {
                    List<Path> filePaths = visitor.getFileList();
                    for(Path p : filePaths)
                    {
                        if (p.equals(file.toPath()))
                            return true;
                    }
                }
            }catch (IOException ioe){
                System.err.println("Error in accepting file: " +file.getAbsolutePath());
                ioe.printStackTrace();
                return false;
            }
        }
        return ret;
    }
    public static boolean acceptFileName(File file, String [] extensions)
    {
        String name = file.getName();
        if (name == null || name.trim().length() == 0)
            return false;
        for(String ext : extensions) {
            if (name.endsWith(ext))
                return true;
            if (name.equals(ext))
                return true;
        }
        return false;
            /*
            char[] path = file.getPath().toCharArray();
            for (char[] extension : extensions)
            {
                if (extension.length > path.length)
                {
                    continue;
                }
                int pStart = path.length - 1;
                int eStart = extension.length - 1;
                boolean success = true;
                for (int i = 0; i <= eStart; i++)
                {
                    if ((path[pStart - i] | 0x20) != (extension[eStart - i] | 0x20))
                    {
                        success = false;
                        break;
                    }
                }
                if (success)
                    return true;
            }
            return false;
             */
    }
}

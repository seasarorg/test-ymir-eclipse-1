package org.seasar.ymir.vili;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

public interface IConfigurator {
    /**
     * このインスタンスの利用を開始します。
     * <p>このメソッドはこのインスタンスが生成された直後に呼び出されます。
     * </p>
     * <p>フラグメント固有のパラメータをカスタマイズしたい場合は、
     * behaviorからpropertiesを取り出して値をセットするようにして下さい。
     * </p>
     * 
     * @param project プロジェクト。
     * このインスタンスが新規Viliプロジェクトウィザードによって生成された場合はnullが渡されます。
     * フラグメントの追加ウィザードによって生成された場合は、
     * 選択されたプロジェクトを表すIProjectインスタンスが渡されます。
     * @param behavior ViliBehaviorインスタンス。
     * @param preferences ViliProjectPreferencesインスタンス。
     */
    void start(IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences);

    /**
     * フラグメントの展開処理の直前に呼び出されます。
     * 
     * @param project フラグメントが追加されるプロジェクトを表すIProjectインスタンス。
     * @param behavior ViliBehaviorインスタンス。
     * @param preferences ViliProjectPreferencesインスタンス。
     * @param parameters フラグメントの展開時に使用されるパラメータ。
     * @param monitor プログレスモニタ。
     */
    void processBeforeExpanding(IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences, Map<String, Object> parameters,
            IProgressMonitor monitor);

    /**
     * 指定されたパスを調整した結果を返します。
     * <p>このメソッドは、リソースの展開先を動的に変更したい場合のために用意されているメソッドです。
     * スケルトンやフラグメントに含まれているリソースのパスはfreemarkerの式になっています。
     * リソースの展開先を動的に変更したい場合は、変更先を示すfreemarkerの式を返すようにして下さい。
     * そうでない場合は引数のパスをそのまま返すようにして下さい。
     * </p>
     * 
     * @param path パス。freemarkerの式になっています。
     * @param project フラグメントが追加されるプロジェクトを表すIProjectインスタンス。
     * @param behavior ViliBehaviorインスタンス。
     * @param preferences ViliProjectPreferencesインスタンス。
     * @param parameters フラグメントの展開時に使用されたパラメータ。
     * @return 展開先を表すパス。freemarkerの式として評価されます。
     */
    String adjustPath(String path, IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences, Map<String, Object> parameters);

    /**
     * 指定されたパスのリソースを展開するかどうかを返します。
     * <p>展開する場合は{@link InclusionType#INCLUDED}を、
     * 展開しない場合は{@link InclusionType#EXCLUDED}を、
     * 展開するかどうかをここでは決定しない場合は{@link InclusionType#UNDEFINED}
     * を返すようにして下さい。
     * </p>
     * <p>このメソッドの返り値はbehavior.propertiesでの
     * expansion.includes指定やexpansion.excludes指定よりも優先されます。
     * </p>
     * 
     * @param path リソースのパス（例：dbflute_${projectName}）。
     * @param resolvedPath リソースのパスを評価したパス（例：dbflute_abc）。
     * @param project フラグメントが追加されるプロジェクトを表すIProjectインスタンス。
     * @param behavior ViliBehaviorインスタンス。
     * @param preferences ViliProjectPreferencesインスタンス。
     * @param parameters フラグメントの展開時に使用されたパラメータ。
     * @return リソースを展開するかどうか。
     */
    InclusionType shouldExpand(String path, String resolvedPath,
            IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences, Map<String, Object> parameters);

    /**
     * フラグメントの展開処理の直後に呼び出されます。
     * 
     * @param project フラグメントが追加されるプロジェクトを表すIProjectインスタンス。
     * @param behavior ViliBehaviorインスタンス。
     * @param preferences ViliProjectPreferencesインスタンス。
     * @param parameters フラグメントの展開時に使用されたパラメータ。
     * @param monitor プログレスモニタ。
     */
    void processAfterExpanded(IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences, Map<String, Object> parameters,
            IProgressMonitor monitor);
}

package org.seasar.ymir.vili;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.seasar.ymir.vili.model.maven.Dependency;

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
     * @param behavior ViliBehaviorインスタンス。nullが渡されることはありません。
     * @param preferences ViliProjectPreferencesインスタンス。nullが渡されることはありません。
     */
    void start(IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences);

    /**
     * フラグメントの展開処理の直前に呼び出されます。
     * 
     * @param project フラグメントが追加されるプロジェクトを表すIProjectインスタンス。nullが渡されることはありません。
     * @param behavior ViliBehaviorインスタンス。nullが渡されることはありません。
     * @param preferences ViliProjectPreferencesインスタンス。nullが渡されることはありません。
     * @param parameters フラグメントの展開時に使用されるパラメータ。nullが渡されることはありません。
     * @param monitor プログレスモニタ。nullが渡されることはありません。
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
     * @param path パス。freemarkerの式になっています。nullが渡されることはありません。
     * @param project フラグメントが追加されるプロジェクトを表すIProjectインスタンス。nullが渡されることはありません。
     * @param behavior ViliBehaviorインスタンス。nullが渡されることはありません。
     * ViliBehaviorが持つプロパティが変更されることは想定していません。
     * もしもプロパティを変更した場合は{@link ViliBehavior#notifyPropertiesChanged()}
     * を呼び出して下さい。
     * @param preferences ViliProjectPreferencesインスタンス。nullが渡されることはありません。
     * @param parameters フラグメントの展開時に使用されたパラメータ。nullが渡されることはありません。
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
     * @param path リソースのパス（例：dbflute_${projectName}）。nullが渡されることはありません。
     * @param resolvedPath リソースのパスを評価したパス（例：dbflute_abc）。
     * @param project フラグメントが追加されるプロジェクトを表すIProjectインスタンス。nullが渡されることはありません。
     * @param behavior ViliBehaviorインスタンス。nullが渡されることはありません。
     * ViliBehaviorが持つプロパティが変更されることは想定していません。
     * もしもプロパティを変更した場合は{@link ViliBehavior#notifyPropertiesChanged()}
     * を呼び出して下さい。
     * @param preferences ViliProjectPreferencesインスタンス。nullが渡されることはありません。
     * @param parameters フラグメントの展開時に使用されたパラメータ。nullが渡されることはありません。
     * @return リソースを展開するかどうか。
     */
    InclusionType shouldExpand(String path, String resolvedPath,
            IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences, Map<String, Object> parameters);

    /**
     * プロジェクトのPOMが持つdependencyとフラグメントのPOMが持つdependencyをマージします。
     * <p>dependencyのマージ処理を独自に行ないたい場合は、このメソッドの中でマージ処理を行なって
     * マージ結果を返すようにして下さい。
     * 独自のマージ処理は行なわず、デフォルトのマージ処理を行なう場合はnullを返すようにして下さい。
     * なおdependencyMapやfragmentDependencyMapの内容を変更してからnullを返すと、
     * 変更されたdependency情報がマージされます。
     * </p>
     * 
     * @param dependencyMap プロジェクトのPOMが持つdependencyが格納されたMap。nullが渡されることはありません。
     * 内容を変更しても構いません。
     * @param fragmentDependencyMap フラグメントのPOMが持つdependencyが格納されたMap。nullが渡されることはありません。
     * 内容を変更しても構いません。
     * @param project フラグメントが追加されるプロジェクトを表すIProjectインスタンス。nullが渡されることはありません。
     * @param behavior ViliBehaviorインスタンス。nullが渡されることはありません。
     * ViliBehaviorが持つプロパティが変更されることは想定していません。
     * もしもプロパティを変更した場合は{@link ViliBehavior#notifyPropertiesChanged()}
     * を呼び出して下さい。
     * @param preferences ViliProjectPreferencesインスタンス。nullが渡されることはありません。
     * @param parameters フラグメントの展開時に使用されたパラメータ。nullが渡されることはありません。
     * @return マージ後のdependencyの配列。
     * @since 0.2.2
     */
    Dependency[] mergePomDependencies(
            Map<Dependency, Dependency> dependencyMap,
            Map<Dependency, Dependency> fragmentDependencyMap,
            IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences, Map<String, Object> parameters);

    /**
     * フラグメントの展開処理の直後に呼び出されます。
     * 
     * @param project フラグメントが追加されるプロジェクトを表すIProjectインスタンス。nullが渡されることはありません。
     * @param behavior ViliBehaviorインスタンス。nullが渡されることはありません。
     * @param preferences ViliProjectPreferencesインスタンス。nullが渡されることはありません。
     * @param parameters フラグメントの展開時に使用されたパラメータ。nullが渡されることはありません。
     * @param monitor プログレスモニタ。nullが渡されることはありません。
     */
    void processAfterExpanded(IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences, Map<String, Object> parameters,
            IProgressMonitor monitor);

    /**
     * 指定されたMoldのパラメータをプロジェクトに保存します。
     * <p>このメソッドは、プロジェクトが持つパラメータを変更するためのProperties画面において
     * 変更されたパラメータの値を保存するためにシステムから呼び出されます。
     * 通常はIPreferenceStoreオブジェクトにパラメータの値を設定してsave()メソッドを呼び出して下さい。
     * </p>
     * 
     * @param project プロジェクト。nullが渡されることはありません。
     * @param mold パラメータを復元する対象であるMold。nullが渡されることはありません。
     * @param preferences ViliProjectPreferencesインスタンス。nullが渡されることはありません。
     * @param parameters 保存すべきパラメータ。nullが渡されることはありません。
     * @param store 指定されたMold用のIPreferenceStoreオブジェクト。
     * @return パラメータを保存できたかどうか。
     * @since 0.2.2
     */
    boolean saveParameters(IProject project, Mold mold,
            ViliProjectPreferences preferences, Map<String, Object> parameters,
            IPersistentPreferenceStore store);

    /**
     * 指定されたMoldのパラメータをプロジェクトから復元します。
     * <p>このメソッドは、プロジェクトが持つパラメータを変更するためのProperties画面で各パラメータの現在の値を埋めるために
     * システムから呼び出されます。
     * </p>
     * 
     * @param project プロジェクト。nullが渡されることはありません。
     * @param mold パラメータを復元する対象であるMold。nullが渡されることはありません。
     * @param preferences ViliProjectPreferencesインスタンス。nullが渡されることはありません。
     * @return 復元されたパラメータを保持するMap。nullを返してはいけません。
     * @since 0.2.2
     */
    Map<String, Object> resumeParameters(IProject project, Mold mold,
            ViliProjectPreferences preferences);
}
